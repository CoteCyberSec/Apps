const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const CHARLIE_UID = "charlie_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("couples").where("memberUids", "array-contains", ALICE_UID).get());
  await assertFails(unauthDb.collection("coupleInvites").doc("A7K9P2").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      preferredLanguage: "en",
      showSecondaryLanguage: true,
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("UserProfile: owner can create and update, cross-user and ghost fields rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      preferredLanguage: "en",
      showSecondaryLanguage: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update including avatarId and nameConfirmed allowed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredLanguage: "ru",
      showSecondaryLanguage: false,
      avatarId: "sunny_cat",
      nameConfirmed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Invalid avatarId characters rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      avatarId: "bad/avatar!id",
      updatedAt: new Date(Date.now() - 800),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject cross-user read/write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked", updatedAt: new Date(Date.now() - 500) }));

  // Reject future timestamp
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      preferredLanguage: "ru",
      showSecondaryLanguage: true,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("CoupleSpace + CoupleInvite atomic creation, invite lookup, partner join, and isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const coupleId = "couple_alice_1";
  const inviteCode = "K9M4P2";

  // Alice atomically creates couple + invite
  const createBatch = aliceDb.batch();
  createBatch.set(aliceDb.collection("couples").doc(coupleId), {
    creatorUid: ALICE_UID,
    memberUids: [ALICE_UID],
    partnerOneName: "Alice",
    partnerTwoName: "",
    inviteCode,
    anniversaryLabel: "",
    createdAt: now,
    updatedAt: now,
  });
  createBatch.set(aliceDb.collection("coupleInvites").doc(inviteCode), {
    coupleId,
    creatorUid: ALICE_UID,
    creatorName: "Alice",
    claimed: false,
    createdAt: now,
    updatedAt: now,
  });
  await assertSucceeds(createBatch.commit());

  // Listing coupleInvites is always forbidden
  await assertFails(bobDb.collection("coupleInvites").get());

  // Bob cannot list couples without matching memberUids
  await assertFails(bobDb.collection("couples").get());
  await assertSucceeds(aliceDb.collection("couples").where("memberUids", "array-contains", ALICE_UID).get());

  // Bob cannot read Alice's couple before joining
  await assertFails(bobDb.collection("couples").doc(coupleId).get());

  // Bob can look up the invite by exact code
  await assertSucceeds(bobDb.collection("coupleInvites").doc(inviteCode).get());

  // Bob cannot join without claiming the invite atomically
  await assertFails(
    bobDb.collection("couples").doc(coupleId).update({
      memberUids: [ALICE_UID, BOB_UID],
      partnerTwoName: "Bob",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob atomically joins couple (with own name & avatar) + marks invite claimed
  const joinNow = new Date(Date.now() - 1000);
  const joinBatch = bobDb.batch();
  joinBatch.update(bobDb.collection("couples").doc(coupleId), {
    memberUids: [ALICE_UID, BOB_UID],
    partnerTwoName: "Bob",
    partnerTwoAvatarId: "starry_owl",
    updatedAt: joinNow,
  });
  joinBatch.update(bobDb.collection("coupleInvites").doc(inviteCode), {
    claimed: true,
    updatedAt: joinNow,
  });
  await assertSucceeds(joinBatch.commit());

  // Third party Charlie cannot join or read couple
  await assertFails(charlieDb.collection("couples").doc(coupleId).get());
  const hijackBatch = charlieDb.batch();
  hijackBatch.update(charlieDb.collection("couples").doc(coupleId), {
    memberUids: [ALICE_UID, CHARLIE_UID],
    partnerTwoName: "Charlie",
    updatedAt: new Date(Date.now() - 500),
  });
  await assertFails(hijackBatch.commit());
});

test("QuestionAnswer & CustomQuestion: couple members CRUD, reaction, sync, and non-member rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const coupleId = "couple_ab_1";
  const created = new Date(Date.now() - 5000);

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("couples").doc(coupleId).set({
      creatorUid: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      partnerOneName: "Alice",
      partnerTwoName: "Bob",
      inviteCode: "Q2W3E4",
      anniversaryLabel: "2 Years",
      createdAt: created,
      updatedAt: created,
    });
  });

  // Alice creates an answer (no @DocumentId `id` key in payload)
  const answerId = "sparks_1_self_alice";
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("answers").doc(answerId).set({
      coupleId,
      questionId: "sparks_1",
      topicId: "sparks",
      authorUid: ALICE_UID,
      authorLabel: "Alice",
      speakerRole: "self",
      text: "The rainy evening in Saint Petersburg when we missed the last tram.",
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject creating an answer with speakerRole 'partner_local' (partners must answer separately)
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).collection("answers").doc("sparks_1_partner_alice").set({
      coupleId,
      questionId: "sparks_1",
      topicId: "sparks",
      authorUid: ALICE_UID,
      authorLabel: "Bob",
      speakerRole: "partner_local",
      text: "Typed on Alice's account",
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject payload containing @DocumentId `id` key
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).collection("answers").doc("bad_doc").set({
      id: "bad_doc",
      coupleId,
      questionId: "sparks_2",
      topicId: "sparks",
      authorUid: ALICE_UID,
      authorLabel: "Alice",
      speakerRole: "self",
      text: "Invalid extra id",
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partner Bob can query answers with memberUids filter; unfiltered fails
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("answers")
      .where("memberUids", "array-contains", BOB_UID)
      .get()
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).collection("answers").get()
  );

  // Partner Bob can react to Alice's answer, but cannot edit Alice's text
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("answers").doc(answerId).update({
      reaction: "❤️",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).collection("answers").doc(answerId).update({
      text: "Altered by Bob",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Author Alice can edit her answer text
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("answers").doc(answerId).update({
      text: "Updated memory with extra detail.",
      authorLabel: "Alice",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Custom question creation and querying
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("customQuestions").doc("cq_1").set({
      coupleId,
      topicId: "custom",
      promptEn: "What song always reminds you of us?",
      promptRu: "Какая песня всегда напоминает тебе о нас?",
      authorUid: BOB_UID,
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("customQuestions")
      .where("memberUids", "array-contains", ALICE_UID)
      .get()
  );

const assert = require("node:assert/strict");
// Non-member Charlie is blocked from reading Alice & Bob's documents or querying them
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("answers").doc(answerId).get());
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("answers").get());
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("answers").where("memberUids", "array-contains", ALICE_UID).get());
  const charlieEmptySnap = await assertSucceeds(
    charlieDb.collection("couples").doc(coupleId).collection("answers").where("memberUids", "array-contains", CHARLIE_UID).get()
  );
  assert.equal(charlieEmptySnap.size, 0);
  await assertFails(
    charlieDb.collection("couples").doc(coupleId).collection("answers").doc("charlie_ans").set({
      coupleId,
      questionId: "sparks_1",
      topicId: "sparks",
      authorUid: CHARLIE_UID,
      authorLabel: "Charlie",
      speakerRole: "self",
      text: "Intruder",
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("DailyDrawing: couple members create, update strokes/caption, react, query, and block non-members", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const coupleId = "couple_ab_draw";
  const created = new Date(Date.now() - 5000);

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("couples").doc(coupleId).set({
      creatorUid: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      partnerOneName: "Alice",
      partnerTwoName: "Bob",
      inviteCode: "D2W3E4",
      anniversaryLabel: "",
      createdAt: created,
      updatedAt: created,
    });
  });

  const drawingId = "2026-11-17_self_alice";
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("drawings").doc(drawingId).set({
      coupleId,
      dateKey: "2026-11-17",
      authorUid: ALICE_UID,
      authorLabel: "Alice",
      speakerRole: "self",
      caption: "Morning heart",
      strokes: ["1|10|200,200;300,300;400,200"],
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject partner_local drawing creation
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).collection("drawings").doc("partner_draw").set({
      coupleId,
      dateKey: "2026-11-17",
      authorUid: ALICE_UID,
      authorLabel: "Bob",
      speakerRole: "partner_local",
      caption: "",
      strokes: ["1|10|200,200;300,300"],
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject empty strokes or invalid dateKey
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).collection("drawings").doc("bad_draw").set({
      coupleId,
      dateKey: "invalid-date",
      authorUid: ALICE_UID,
      authorLabel: "Alice",
      speakerRole: "self",
      caption: "",
      strokes: ["1|10|200,200;300,300"],
      reaction: "",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partner Bob queries drawings with memberUids filter and reacts
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("drawings")
      .where("memberUids", "array-contains", BOB_UID)
      .get()
  );
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("drawings").doc(drawingId).update({
      reaction: "❤️",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partner Bob cannot overwrite Alice's strokes
  await assertFails(
    bobDb.collection("couples").doc(coupleId).collection("drawings").doc(drawingId).update({
      strokes: ["2|10|100,100;500,500"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Alice can update her own strokes and caption
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("drawings").doc(drawingId).update({
      strokes: ["1|10|200,200;300,300;400,200", "0|8|500,500;600,600"],
      caption: "Morning heart + smile",
      authorLabel: "Alice",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Non-member Charlie cannot read or write drawings
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("drawings").doc(drawingId).get());
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("drawings").get());

  // Couple members can update next reunion date & location, invalid date fails
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).update({
      nextMeetDateKey: "2026-12-24",
      nextMeetLocation: "Paris, France",
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).update({
      nextMeetDateKey: "24/12/2026",
      nextMeetLocation: "Paris",
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Each partner can update only their own avatar slot
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).update({
      partnerOneAvatarId: "cozy_bear",
      updatedAt: new Date(Date.now() - 80),
    })
  );
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).update({
      partnerTwoAvatarId: "dreamy_fox",
      updatedAt: new Date(Date.now() - 70),
    })
  );
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).update({
      partnerTwoAvatarId: "starry_owl",
      updatedAt: new Date(Date.now() - 60),
    })
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).update({
      partnerOneAvatarId: "matcha_frog",
      updatedAt: new Date(Date.now() - 50),
    })
  );

  // Either partner can update the shared couple aesthetic (preset or custom hex), invalid formats fail
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).update({
      aestheticThemeId: "sage_matcha",
      updatedAt: new Date(Date.now() - 40),
    })
  );
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).update({
      aestheticThemeId: "custom_D96B43",
      updatedAt: new Date(Date.now() - 30),
    })
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).update({
      aestheticThemeId: "custom_ZZZZZZ",
      updatedAt: new Date(Date.now() - 20),
    })
  );
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).update({
      aestheticThemeId: "bad/theme!",
      updatedAt: new Date(Date.now() - 10),
    })
  );
  await assertFails(
    charlieDb.collection("couples").doc(coupleId).update({
      aestheticThemeId: "midnight_velvet",
      updatedAt: new Date(Date.now() - 5),
    })
  );

  // Each partner can update only their own name (alongside avatar), not their partner's name
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).update({
      partnerOneName: "Alicia",
      partnerOneAvatarId: "sunny_cat",
      updatedAt: new Date(Date.now() - 4),
    })
  );
  await assertFails(
    aliceDb.collection("couples").doc(coupleId).update({
      partnerTwoName: "Bobby",
      updatedAt: new Date(Date.now() - 3),
    })
  );
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).update({
      partnerTwoName: "Bobby",
      partnerTwoAvatarId: "dreamy_fox",
      updatedAt: new Date(Date.now() - 2),
    })
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).update({
      partnerOneName: "Hack",
      updatedAt: new Date(Date.now() - 1),
    })
  );

  // Relationship start date can be updated by couple members; invalid date format rejected
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).update({
      relationshipStartDateKey: "2024-02-14",
      updatedAt: new Date(Date.now() - 1),
    })
  );
  await assertFails(
    bobDb.collection("couples").doc(coupleId).update({
      relationshipStartDateKey: "14/02/2024",
      updatedAt: new Date(Date.now() - 1),
    })
  );
});

test("TripMemory & TripPhoto: couple members create, update, query by memberUids, and reject non-members", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const coupleId = "couple_ab_trips";
  const created = new Date(Date.now() - 5000);

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("couples").doc(coupleId).set({
      creatorUid: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      partnerOneName: "Alice",
      partnerTwoName: "Bob",
      inviteCode: "T2R3I4",
      anniversaryLabel: "",
      createdAt: created,
      updatedAt: created,
    });
  });

  const tripId = "trip_paris_2025";
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("trips").doc(tripId).set({
      coupleId,
      title: "Paris Autumn Reunion",
      location: "Paris",
      startDateKey: "2025-10-12",
      endDateKey: "2025-10-16",
      notes: "Walked along the Seine and ate croissants every morning.",
      authorUid: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob can update shared trip notes and upload a photo with EXIF metadata
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("trips").doc(tripId).update({
      notes: "Walked along the Seine, visited the Louvre, and watched sunset at Montmartre.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  const photoId = "photo_1";
  await assertSucceeds(
    bobDb.collection("couples").doc(coupleId).collection("tripPhotos").doc(photoId).set({
      coupleId,
      tripId,
      takenDateKey: "2025-10-13",
      takenTimeLabel: "18:45",
      takenMillis: 1760381100000,
      timestampSource: "exif",
      caption: "Sunset at Montmartre",
      thumbnailBase64: "/9j/4AAQSkZJRgABAQAAAQABAAD/2w==",
      reaction: "",
      authorUid: BOB_UID,
      authorLabel: "Bob",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Alice can query trip photos with memberUids filter, edit caption, and react with custom emoji
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("tripPhotos")
      .where("memberUids", "array-contains", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("tripPhotos").doc(photoId).update({
      caption: "Golden hour at Montmartre!",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    aliceDb.collection("couples").doc(coupleId).collection("tripPhotos").doc(photoId).update({
      reaction: "🫶",
      updatedAt: new Date(Date.now() - 300),
    })
  );

  // Non-member Charlie is blocked
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("trips").doc(tripId).get());
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("tripPhotos").doc(photoId).get());
  await assertFails(charlieDb.collection("couples").doc(coupleId).collection("tripPhotos").get());
});
