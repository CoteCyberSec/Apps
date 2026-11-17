package com.example.model

enum class TopicAccent {
    BURGUNDY,
    TERRACOTTA,
    SAGE,
    GOLD
}

data class PromptQuestion(
    val id: String,
    val topicId: String,
    val text: BilingualText,
    val isCustom: Boolean = false
)

data class TopicDeck(
    val id: String,
    val title: BilingualText,
    val subtitle: BilingualText,
    val accent: TopicAccent,
    val emojiBadge: String,
    val questions: List<PromptQuestion>
)

object TopicCatalog {
    val decks: List<TopicDeck> = listOf(
        TopicDeck(
            id = "sparks",
            title = BilingualText(
                en = "First Sparks & Memories",
                ru = "Первые искры и память"
            ),
            subtitle = BilingualText(
                en = "How we met, early butterflies, and moments we still smile about",
                ru = "Как мы встретились, первые чувства и тёплые воспоминания"
            ),
            accent = TopicAccent.BURGUNDY,
            emojiBadge = "✨",
            questions = listOf(
                PromptQuestion(
                    id = "sparks_1",
                    topicId = "sparks",
                    text = BilingualText(
                        en = "What was the very first tiny detail you noticed about me?",
                        ru = "Какую самую первую мелочь ты заметил(а) во мне при знакомстве?"
                    )
                ),
                PromptQuestion(
                    id = "sparks_2",
                    topicId = "sparks",
                    text = BilingualText(
                        en = "Which of our early dates felt like a scene from a movie?",
                        ru = "Какое из наших первых свиданий было похоже на сцену из фильма?"
                    )
                ),
                PromptQuestion(
                    id = "sparks_3",
                    topicId = "sparks",
                    text = BilingualText(
                        en = "When did you realize I wasn't just someone you liked, but your person?",
                        ru = "В какой момент ты понял(а), что я — именно твой человек?"
                    )
                ),
                PromptQuestion(
                    id = "sparks_4",
                    topicId = "sparks",
                    text = BilingualText(
                        en = "What song, smell, or season instantly takes you back to our beginning?",
                        ru = "Какая песня, запах или время года мгновенно возвращают тебя в наше начало?"
                    )
                ),
                PromptQuestion(
                    id = "sparks_5",
                    topicId = "sparks",
                    text = BilingualText(
                        en = "What funny misunderstanding or awkward moment from early on do you secretly love?",
                        ru = "Какой неловкий или смешной случай из начала наших отношений ты вспоминаешь с теплом?"
                    )
                ),
                PromptQuestion(
                    id = "sparks_6",
                    topicId = "sparks",
                    text = BilingualText(
                        en = "If we could relive one ordinary day from our past together, which day would you pick?",
                        ru = "Если бы можно было заново прожить один обычный день из нашего прошлого, какой бы ты выбрал(а)?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "deep",
            title = BilingualText(
                en = "Deep Talks & Inner Worlds",
                ru = "Глубокие разговоры"
            ),
            subtitle = BilingualText(
                en = "Values, fears, unspoken wishes, and feeling truly understood",
                ru = "Ценности, переживания, тайные мечты и искреннее понимание"
            ),
            accent = TopicAccent.SAGE,
            emojiBadge = "🌿",
            questions = listOf(
                PromptQuestion(
                    id = "deep_1",
                    topicId = "deep",
                    text = BilingualText(
                        en = "When do you feel most emotionally safe and completely yourself with me?",
                        ru = "Когда рядом со мной ты чувствуешь себя в полной безопасности и можешь быть собой?"
                    )
                ),
                PromptQuestion(
                    id = "deep_2",
                    topicId = "deep",
                    text = BilingualText(
                        en = "What is something you've been carrying quietly lately that I can help lighten?",
                        ru = "Что в последнее время занимает твои мысли, и как я могу поддержать тебя?"
                    )
                ),
                PromptQuestion(
                    id = "deep_3",
                    topicId = "deep",
                    text = BilingualText(
                        en = "How has being together gently changed the way you see yourself?",
                        ru = "Как наши отношения изменили то, каким(ой) ты видишь себя?"
                    )
                ),
                PromptQuestion(
                    id = "deep_4",
                    topicId = "deep",
                    text = BilingualText(
                        en = "When we disagree, what helps you feel heard rather than misunderstood?",
                        ru = "Когда мы спорим, что помогает тебе почувствовать, что тебя действительно слышат?"
                    )
                ),
                PromptQuestion(
                    id = "deep_5",
                    topicId = "deep",
                    text = BilingualText(
                        en = "What childhood dream or wish still matters deeply to you today?",
                        ru = "Какая детская мечта или желание до сих пор важны для тебя?"
                    )
                ),
                PromptQuestion(
                    id = "deep_6",
                    topicId = "deep",
                    text = BilingualText(
                        en = "What do you hope people say about the way we love each other?",
                        ru = "Что бы тебе хотелось, чтобы говорили о том, как мы любим друг друга?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "rituals",
            title = BilingualText(
                en = "Daily Rituals & Quirks",
                ru = "Привычки и мелочи"
            ),
            subtitle = BilingualText(
                en = "Morning coffee, cute habits, and the magic of everyday life",
                ru = "Утренний кофе, милые привычки и уют обычных дней"
            ),
            accent = TopicAccent.GOLD,
            emojiBadge = "☕",
            questions = listOf(
                PromptQuestion(
                    id = "rituals_1",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "What tiny everyday habit of mine secretly makes you smile?",
                        ru = "Какая моя маленькая повседневная привычка заставляет тебя улыбаться?"
                    )
                ),
                PromptQuestion(
                    id = "rituals_2",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "Describe our ideal lazy Sunday morning from waking up to noon.",
                        ru = "Опиши наше идеальное ленивое воскресное утро от пробуждения до обеда."
                    )
                ),
                PromptQuestion(
                    id = "rituals_3",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "What meal or snack instantly feels like 'home' when we share it?",
                        ru = "Какое блюдо или перекус сразу создаёт ощущение дома, когда мы едим его вместе?"
                    )
                ),
                PromptQuestion(
                    id = "rituals_4",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "What new weekly ritual should we start together this month?",
                        ru = "Какой новый еженедельный ритуал нам стоит завести в этом месяце?"
                    )
                ),
                PromptQuestion(
                    id = "rituals_5",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "What phrase or inside joke do we say that nobody else would understand?",
                        ru = "Какую нашу общую шутку или фразу никто вокруг не понял бы?"
                    )
                ),
                PromptQuestion(
                    id = "rituals_6",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "At the end of a long day, what simple gesture from me recharges you fastest?",
                        ru = "После тяжёлого дня какой мой простой жест быстрее всего возвращает тебе силы?"
                    )
                ),
                PromptQuestion(
                    id = "rituals_7",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "What is your favorite video-call or voice-note ritual when we are miles apart?",
                        ru = "Какой твой любимый ритуал для наших видеозвонков или голосовых на расстоянии?"
                    )
                ),
                PromptQuestion(
                    id = "rituals_8",
                    topicId = "rituals",
                    text = BilingualText(
                        en = "When we fall asleep on a call or say goodnight across time zones, what makes it feel cozy?",
                        ru = "Что делает наши «спокойной ночи» по телефону или в разных часовых поясах особенно тёплыми?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "future",
            title = BilingualText(
                en = "Future & Adventures",
                ru = "Будущее и мечты"
            ),
            subtitle = BilingualText(
                en = "Places to explore, cozy homes, and the chapters ahead",
                ru = "Путешествия, дом мечты и главы, которые нас ждут"
            ),
            accent = TopicAccent.TERRACOTTA,
            emojiBadge = "🧭",
            questions = listOf(
                PromptQuestion(
                    id = "future_1",
                    topicId = "future",
                    text = BilingualText(
                        en = "Where in the world do you most want to wake up together next?",
                        ru = "В какой точке мира тебе больше всего хочется проснуться вдвоём?"
                    )
                ),
                PromptQuestion(
                    id = "future_2",
                    topicId = "future",
                    text = BilingualText(
                        en = "Picture our home five years from now—what is your favorite corner in it?",
                        ru = "Представь наш дом через пять лет — какой уголок в нём будет твоим любимым?"
                    )
                ),
                PromptQuestion(
                    id = "future_3",
                    topicId = "future",
                    text = BilingualText(
                        en = "What skill, hobby, or language would be fun for us to learn as a team?",
                        ru = "Какому навыку, хобби или языку было бы здорово научиться нам вдвоём?"
                    )
                ),
                PromptQuestion(
                    id = "future_4",
                    topicId = "future",
                    text = BilingualText(
                        en = "What milestone do you want us to celebrate in an unforgettable way this year?",
                        ru = "Какое событие этого года тебе хочется отпраздновать по-настоящему незабываемо?"
                    )
                ),
                PromptQuestion(
                    id = "future_5",
                    topicId = "future",
                    text = BilingualText(
                        en = "If we had a spontaneous free weekend with zero obligations, where would we go?",
                        ru = "Если бы у нас внезапно освободились все выходные без дел, куда бы мы поехали?"
                    )
                ),
                PromptQuestion(
                    id = "future_6",
                    topicId = "future",
                    text = BilingualText(
                        en = "What tradition do you hope we are still keeping 20 years from today?",
                        ru = "Какую традицию ты хотел(а) бы сохранить и через 20 лет?"
                    )
                ),
                PromptQuestion(
                    id = "future_7",
                    topicId = "future",
                    text = BilingualText(
                        en = "What will the first ordinary week living in the same city feel like for us?",
                        ru = "Какой будет наша первая обычная неделя, когда мы наконец будем жить в одном городе?"
                    )
                ),
                PromptQuestion(
                    id = "future_8",
                    topicId = "future",
                    text = BilingualText(
                        en = "When we finally close the distance for good, what is the very first celebratory thing we should do?",
                        ru = "Когда расстояние между нами исчезнет навсегда, как мы отпразднуем это в самый первый вечер?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "love",
            title = BilingualText(
                en = "Love & Appreciation",
                ru = "Любовь и нежность"
            ),
            subtitle = BilingualText(
                en = "Love languages, gratitude, and celebrating what makes us shine",
                ru = "Языки любви, благодарность и нежные признания"
            ),
            accent = TopicAccent.BURGUNDY,
            emojiBadge = "💌",
            questions = listOf(
                PromptQuestion(
                    id = "love_1",
                    topicId = "love",
                    text = BilingualText(
                        en = "What is one thing I did recently that made you feel deeply loved?",
                        ru = "Что из того, что я недавно сделал(а), дало тебе почувствовать, как сильно я тебя люблю?"
                    )
                ),
                PromptQuestion(
                    id = "love_2",
                    topicId = "love",
                    text = BilingualText(
                        en = "Which of the five love languages fills your heart the fastest right now?",
                        ru = "Какой язык любви сейчас наполняет твоё сердце быстрее всего?"
                    )
                ),
                PromptQuestion(
                    id = "love_3",
                    topicId = "love",
                    text = BilingualText(
                        en = "What compliment from me has stayed with you the longest?",
                        ru = "Какой мой комплимент запомнился тебе сильнее всего?"
                    )
                ),
                PromptQuestion(
                    id = "love_4",
                    topicId = "love",
                    text = BilingualText(
                        en = "What quality in me are you proudest of?",
                        ru = "Каким моим качеством ты гордишься больше всего?"
                    )
                ),
                PromptQuestion(
                    id = "love_5",
                    topicId = "love",
                    text = BilingualText(
                        en = "How do you like to be comforted when you feel overwhelmed?",
                        ru = "Как тебе хочется, чтобы я утешал(а) тебя, когда тебе тяжело или тревожно?"
                    )
                ),
                PromptQuestion(
                    id = "love_6",
                    topicId = "love",
                    text = BilingualText(
                        en = "Write a three-word thank-you note to me for this week.",
                        ru = "Напиши мне короткое «спасибо» за эту неделю в трёх словах."
                    )
                )
            )
        ),
        TopicDeck(
            id = "playful",
            title = BilingualText(
                en = "Playful & Spicy Debates",
                ru = "Весёлые дилеммы"
            ),
            subtitle = BilingualText(
                en = "Lighthearted hypotheticals, funny debates, and playful sparks",
                ru = "Лёгкие споры, неожиданные сценарии и повод посмеяться"
            ),
            accent = TopicAccent.TERRACOTTA,
            emojiBadge = "🎲",
            questions = listOf(
                PromptQuestion(
                    id = "playful_1",
                    topicId = "playful",
                    text = BilingualText(
                        en = "If we swapped jobs and daily routines for a week, who would survive better?",
                        ru = "Если бы мы поменялись делами и работой на неделю, кто бы справился лучше?"
                    )
                ),
                PromptQuestion(
                    id = "playful_2",
                    topicId = "playful",
                    text = BilingualText(
                        en = "Who takes longer to pick a movie—and what is our unspoken tiebreaker rule?",
                        ru = "Кто из нас дольше выбирает фильм — и как нам честно решать этот спор?"
                    )
                ),
                PromptQuestion(
                    id = "playful_3",
                    topicId = "playful",
                    text = BilingualText(
                        en = "If we opened a cozy cafe or tiny shop together, what would we name it and sell?",
                        ru = "Если бы мы открыли уютную кофейню или лавку вдвоём, как бы она называлась и что бы там было?"
                    )
                ),
                PromptQuestion(
                    id = "playful_4",
                    topicId = "playful",
                    text = BilingualText(
                        en = "Which fictional couple reminds you most of our dynamic?",
                        ru = "Какая пара из фильмов или книг больше всего похожа на нас?"
                    )
                ),
                PromptQuestion(
                    id = "playful_5",
                    topicId = "playful",
                    text = BilingualText(
                        en = "Plan a surprise date for me using only three clues!",
                        ru = "Загадай для меня идеальное свидание-сюрприз всего тремя подсказками!"
                    )
                ),
                PromptQuestion(
                    id = "playful_6",
                    topicId = "playful",
                    text = BilingualText(
                        en = "What is the funniest hill you are always willing to die on?",
                        ru = "О какой смешной мелочи ты всегда готов(а) спорить до последнего?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "ldr_miles",
            title = BilingualText(
                en = "Across the Miles",
                ru = "Любовь на расстоянии"
            ),
            subtitle = BilingualText(
                en = "Staying close across time zones, missing each other, and virtual dates",
                ru = "Как чувствовать близость через километры, звонки и часовые пояса"
            ),
            accent = TopicAccent.BURGUNDY,
            emojiBadge = "🌍",
            questions = listOf(
                PromptQuestion(
                    id = "ldr_miles_1",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "At what exact moment of the day do you miss me the most when we are apart?",
                        ru = "В какой момент дня ты скучаешь по мне сильнее всего, когда мы далеко друг от друга?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_2",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "What kind of text, selfie, or voice message from me instantly brightens a lonely day?",
                        ru = "Какое сообщение, фото или голосовое от меня мгновенно согревает тебя в одинокий день?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_3",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "What virtual date idea (movie night, cooking on video, online game) should we try next?",
                        ru = "Какое онлайн-свидание (кино вместе, готовка по видео, игра) давай устроим в ближайшие дни?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_4",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "When our schedules or time zones clash, how can I reassure you that you are still my priority?",
                        ru = "Когда наши графики или часовые пояса не совпадают, как мне показать, что ты всегда на первом месте?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_5",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "What little object, hoodie, or photo of ours helps you feel connected to me right now?",
                        ru = "Какая вещь, одежда или фотография помогает тебе чувствовать меня рядом прямо сейчас?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_6",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "If you could teleport to my side for just 30 minutes tonight, what would we do?",
                        ru = "Если бы ты мог(ла) телепортироваться ко мне всего на 30 минут сегодня вечером, что бы мы сделали?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_7",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "What has long distance actually made stronger between the two of us?",
                        ru = "Что расстояние на самом деле сделало сильнее в наших отношениях?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_miles_8",
                    topicId = "ldr_miles",
                    text = BilingualText(
                        en = "What small care-package item or handwritten surprise would make you smile if it arrived in the mail?",
                        ru = "Какая маленькая посылка или открытка по почте вызвала бы у тебя самую искреннюю улыбку?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "ldr_reunions",
            title = BilingualText(
                en = "Reunions & Counting Days",
                ru = "Встречи и ожидание"
            ),
            subtitle = BilingualText(
                en = "Airport hugs, planning visits, and soaking up every minute together",
                ru = "Объятия в аэропорту, планы на поездки и каждая минута рядом"
            ),
            accent = TopicAccent.SAGE,
            emojiBadge = "✈️",
            questions = listOf(
                PromptQuestion(
                    id = "ldr_reunions_1",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "What is the very first thing you want us to do in the first hour after our next reunion hug?",
                        ru = "Что самое первое тебе хочется сделать в первый час после того, как мы обнимемся при встрече?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_2",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "Looking back at our past trips together, which quiet unscripted moment do you treasure most?",
                        ru = "Вспоминая наши прошлые поездки, какой тихий, незапланированный момент ты бережёшь в памяти больше всего?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_3",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "What helps you handle the tough goodbye feeling right after a visit ends?",
                        ru = "Что помогает тебе пережить грусть прощания сразу после окончания нашей встречи?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_4",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "During our next visit, do you crave packed adventures or slow cozy days indoors—or what mix?",
                        ru = "В нашу следующую встречу тебе больше хочется насыщенных прогулок или ленивых уютных дней вдвоём?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_5",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "What photo from one of our past meetups captures 'us' better than any other?",
                        ru = "Какое фото из наших прошлых встреч лучше всего передаёт то, какие мы настоящие?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_6",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "What food, cafe, or neighborhood do you want me to show you—or revisit with you—next time?",
                        ru = "Какое место, кафе или блюдо ты хочешь показать мне или обязательно повторить в следующий раз?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_7",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "How do you feel in the last 3 days on the countdown right before we finally see each other?",
                        ru = "Что ты чувствуешь в последние 3 дня обратного отсчёта прямо перед нашей встречей?"
                    )
                ),
                PromptQuestion(
                    id = "ldr_reunions_8",
                    topicId = "ldr_reunions",
                    text = BilingualText(
                        en = "What promise do we want to keep for every future meetup so we never take a day together for granted?",
                        ru = "Какое обещание мы дадим себе для каждой будущей встречи, чтобы ценить каждый день рядом?"
                    )
                )
            )
        ),
        TopicDeck(
            id = "custom",
            title = BilingualText(
                en = "Our Custom Prompts",
                ru = "Наши личные вопросы"
            ),
            subtitle = BilingualText(
                en = "Questions written by the two of you—add anything on your mind",
                ru = "Вопросы, придуманные вами двоими — добавляйте свои темы"
            ),
            accent = TopicAccent.GOLD,
            emojiBadge = "✍️",
            questions = emptyList()
        )
    )

    val allBuiltInQuestions: List<PromptQuestion> = decks.flatMap { it.questions }

    fun findTopic(topicId: String): TopicDeck? = decks.firstOrNull { it.id == topicId }

    fun findBuiltInQuestion(questionId: String): PromptQuestion? =
        allBuiltInQuestions.firstOrNull { it.id == questionId }
}
