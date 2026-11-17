package com.example.model

enum class AppLanguage(val code: String, val badge: String) {
    EN("en", "EN"),
    RU("ru", "RU");

    fun other(): AppLanguage = if (this == EN) RU else EN

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

data class BilingualText(
    val en: String,
    val ru: String
) {
    fun primary(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> en
        AppLanguage.RU -> ru
    }

    fun secondary(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> ru
        AppLanguage.RU -> en
    }
}

object UiStrings {
    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Duet"
        AppLanguage.RU -> "Дуэт"
    }

    fun authHeadline(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Meaningful topics\nfor the two of you"
        AppLanguage.RU -> "Тёплые темы\nдля разговоров вдвоём"
    }

    fun authSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Answer thoughtful questions together in English & Russian, on one phone or synced across both devices."
        AppLanguage.RU -> "Отвечайте на глубокие и весёлые вопросы на английском и русском — вместе с одного телефона или на двух устройствах."
    }

    fun signInWithGoogle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Sign in with Google"
        AppLanguage.RU -> "Войти через Google"
    }

    fun tabTopics(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Topics"
        AppLanguage.RU -> "Темы"
    }

    fun tabJournal(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Our Story"
        AppLanguage.RU -> "Журнал"
    }

    fun tabCouple(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Couple"
        AppLanguage.RU -> "Пара"
    }

    fun dailySparkLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "TONIGHT'S PROMPT"
        AppLanguage.RU -> "ВОПРОС ВЕЧЕРА"
    }

    fun answerNow(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Answer together"
        AppLanguage.RU -> "Ответить вместе"
    }

    fun shufflePrompt(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Shuffle"
        AppLanguage.RU -> "Другой"
    }

    fun topicsHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Conversation Decks"
        AppLanguage.RU -> "Темы для разговора"
    }

    fun answeredProgress(answered: Int, total: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "$answered of $total answered"
        AppLanguage.RU -> "Отвечено $answered из $total"
    }

    fun questionsCount(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "$count questions"
        AppLanguage.RU -> "$count вопросов"
    }

    fun dualLanguageLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Show Russian under English"
        AppLanguage.RU -> "Показывать перевод (EN)"
    }

    fun dualToggleShort(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "EN+RU"
        AppLanguage.RU -> "RU+EN"
    }

    fun filterAll(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "All"
        AppLanguage.RU -> "Все"
    }

    fun filterUnanswered(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "To Answer"
        AppLanguage.RU -> "Без ответа"
    }

    fun filterBothAnswered(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Both Answered"
        AppLanguage.RU -> "Ответили оба"
    }

    fun yourAnswer(name: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "$name (You)"
        AppLanguage.RU -> "$name (Вы)"
    }

    fun partnerAnswer(name: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> name.ifBlank { "Partner" }
        AppLanguage.RU -> name.ifBlank { "Партнёр" }
    }

    fun passPhoneHint(partnerName: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "On one phone? Switch below to record ${partnerName.ifBlank { "your partner" }}'s answer."
        AppLanguage.RU -> "На одном телефоне? Переключитесь ниже, чтобы записать ответ (${partnerName.ifBlank { "партнёр" }})."
    }

    fun answerPlaceholder(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Write your honest thought..."
        AppLanguage.RU -> "Напишите свой искренний ответ..."
    }

    fun saveAnswer(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save Answer"
        AppLanguage.RU -> "Сохранить ответ"
    }

    fun updateAnswer(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Update Answer"
        AppLanguage.RU -> "Обновить ответ"
    }

    fun savedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Saved to your couple journal"
        AppLanguage.RU -> "Сохранено в ваш общий журнал"
    }

    fun waitingPartner(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Waiting for partner's answer"
        AppLanguage.RU -> "Ожидаем ответ партнёра"
    }

    fun bothAnsweredBadge(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Both answered"
        AppLanguage.RU -> "Ответили оба"
    }

    fun addQuestionButton(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add Question"
        AppLanguage.RU -> "Добавить вопрос"
    }

    fun addQuestionTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Create Your Own Prompt"
        AppLanguage.RU -> "Свой вопрос для пары"
    }

    fun promptEnLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Question in English"
        AppLanguage.RU -> "Вопрос на английском"
    }

    fun promptRuLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Question in Russian"
        AppLanguage.RU -> "Вопрос на русском"
    }

    fun createLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Create"
        AppLanguage.RU -> "Создать"
    }

    fun cancelLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Cancel"
        AppLanguage.RU -> "Отмена"
    }

    fun journalTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Our Shared Story"
        AppLanguage.RU -> "Наша история"
    }

    fun journalEmptyTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Your story begins with one question"
        AppLanguage.RU -> "Ваша история начнётся с первого ответа"
    }

    fun journalEmptySubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Pick any topic deck and share your first thoughts together."
        AppLanguage.RU -> "Выберите любую тему и поделитесь своими первыми мыслями."
    }

    fun coupleSpaceTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Your Couple Space"
        AppLanguage.RU -> "Пространство пары"
    }

    fun connectedStatus(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Synced across 2 devices"
        AppLanguage.RU -> "Синхронизировано на 2 устройствах"
    }

    fun soloOrLocalStatus(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Share your invite code so your partner can join"
        AppLanguage.RU -> "Поделитесь кодом, чтобы партнёр присоединился"
    }

    fun inviteCodeLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Partner Invite Code"
        AppLanguage.RU -> "Код приглашения для партнёра"
    }

    fun copyCode(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Copy"
        AppLanguage.RU -> "Копировать"
    }

    fun codeCopied(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Invite code copied"
        AppLanguage.RU -> "Код скопирован"
    }

    fun joinPartnerTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Join a Partner's Space"
        AppLanguage.RU -> "Присоединиться к партнёру"
    }

    fun joinCodeHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "6-letter invite code"
        AppLanguage.RU -> "6-значный код"
    }

    fun joinButton(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Join"
        AppLanguage.RU -> "Войти"
    }

    fun partnerNamesTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Names & Milestone"
        AppLanguage.RU -> "Имена и ваша дата"
    }

    fun partnerOneField(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Partner 1 Name"
        AppLanguage.RU -> "Имя партнёра 1"
    }

    fun partnerTwoField(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Partner 2 Name"
        AppLanguage.RU -> "Имя партнёра 2"
    }

    fun milestoneField(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Milestone (e.g. Together 2 years)"
        AppLanguage.RU -> "Подпись (напр. Вместе 2 года)"
    }

    fun saveProfile(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save Details"
        AppLanguage.RU -> "Сохранить"
    }

    fun languageSettingsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Language & Display"
        AppLanguage.RU -> "Язык и отображение"
    }

    fun signOut(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Sign Out"
        AppLanguage.RU -> "Выйти из аккаунта"
    }

    fun retry(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Retry"
        AppLanguage.RU -> "Повторить"
    }

    fun prevQuestion(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Previous"
        AppLanguage.RU -> "Назад"
    }

    fun nextQuestion(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Next"
        AppLanguage.RU -> "Дальше"
    }

    fun reactLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "React"
        AppLanguage.RU -> "Реакция"
    }

    fun tabDraw(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Draw"
        AppLanguage.RU -> "Рисунки"
    }

    fun drawHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Daily Canvas"
        AppLanguage.RU -> "Рисунок дня"
    }

    fun drawSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Draw a little picture or doodle for each other today"
        AppLanguage.RU -> "Нарисуйте друг другу милую картинку на память сегодня"
    }

    fun drawCaptionHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add a sweet note (optional)..."
        AppLanguage.RU -> "Короткая подпись к рисунку (по желанию)..."
    }

    fun undoStroke(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Undo"
        AppLanguage.RU -> "Назад"
    }

    fun clearCanvas(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Clear"
        AppLanguage.RU -> "Очистить"
    }

    fun saveDrawing(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save Today's Drawing"
        AppLanguage.RU -> "Сохранить рисунок"
    }

    fun updateDrawing(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Update Today's Drawing"
        AppLanguage.RU -> "Обновить рисунок"
    }

    fun drawingSaved(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Drawing saved for today"
        AppLanguage.RU -> "Рисунок на сегодня сохранён"
    }

    fun savedDrawingsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Saved Daily Pictures"
        AppLanguage.RU -> "Сохранённые рисунки"
    }

    fun noDrawingsYet(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "No drawings saved yet—sketch your first little gift above!"
        AppLanguage.RU -> "Пока нет рисунков — нарисуйте первый подарок выше!"
    }

    fun deleteLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Delete"
        AppLanguage.RU -> "Удалить"
    }

    fun fullScreenDraw(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Full screen"
        AppLanguage.RU -> "На весь экран"
    }

    fun exitFullScreenDraw(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Minimize"
        AppLanguage.RU -> "Свернуть"
    }

    fun reunionTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "NEXT REUNION"
        AppLanguage.RU -> "ДО ВСТРЕЧИ"
    }

    fun reunionSectionTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Next Time We See Each Other"
        AppLanguage.RU -> "Когда мы увидимся снова"
    }

    fun reunionEmptyTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Set your next reunion date & place"
        AppLanguage.RU -> "Укажите дату и место следующей встречи"
    }

    fun reunionEmptySubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Count down every day until you are together again."
        AppLanguage.RU -> "Отсчитывайте дни до момента, когда вы снова будете рядом."
    }

    fun reunionDaysLeft(days: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> if (days == 1) "1 day left" else "$days days left"
        AppLanguage.RU -> {
            val mod100 = days % 100
            val mod10 = days % 10
            val word = when {
                mod100 in 11..14 -> "дней"
                mod10 == 1 -> "день"
                mod10 in 2..4 -> "дня"
                else -> "дней"
            }
            "$days $word"
        }
    }

    fun reunionToday(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Today is the day!"
        AppLanguage.RU -> "Встреча уже сегодня!"
    }

    fun reunionPassed(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Date passed • Tap to plan the next one"
        AppLanguage.RU -> "Дата прошла • Нажмите, чтобы выбрать новую"
    }

    fun pickDateLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Pick Date"
        AppLanguage.RU -> "Выбрать дату"
    }

    fun reunionLocationHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Where will you meet? (e.g. Paris, Airport)"
        AppLanguage.RU -> "Где встретитесь? (напр. Москва, Аэропорт)"
    }

    fun saveReunion(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save Countdown"
        AppLanguage.RU -> "Сохранить отсчёт"
    }

    fun clearReunion(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Clear"
        AppLanguage.RU -> "Сбросить"
    }

    fun editLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Edit"
        AppLanguage.RU -> "Изменить"
    }

    fun partnerUpdatesTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Partner Answered"
        AppLanguage.RU -> "Ответы партнёра"
    }

    fun bottomNewAnswers(count: Int, latestPrompt: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> if (count == 1) "New answer: $latestPrompt" else "$count new partner answers • $latestPrompt"
        AppLanguage.RU -> if (count == 1) "Новый ответ: $latestPrompt" else "Новых ответов ($count) • $latestPrompt"
    }

    fun bottomRecentAnswers(count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> "$count partner answers • Tap to view"
        AppLanguage.RU -> "Ответов партнёра: $count • Нажмите"
    }

    fun markAllRead(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Mark all read"
        AppLanguage.RU -> "Прочитать всё"
    }

    fun newBadge(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "NEW"
        AppLanguage.RU -> "НОВОЕ"
    }

    fun openQuestion(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Open"
        AppLanguage.RU -> "Открыть"
    }

    fun enableNotifications(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Turn on alerts"
        AppLanguage.RU -> "Включить уведомления"
    }

    fun notificationsEnabled(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Notifications on"
        AppLanguage.RU -> "Уведомления включены"
    }

    fun noPartnerAnswersYet(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "When your partner answers any question, it will appear here."
        AppLanguage.RU -> "Когда партнёр ответит на вопрос, уведомление появится здесь."
    }

    fun scrollPromptsHint(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Scroll through $count prompts • Tap any card to write"
        AppLanguage.RU -> "Листайте все вопросы ($count) • Нажмите на карточку, чтобы ответить"
    }

    fun writeAnswerAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Write your answer"
        AppLanguage.RU -> "Ответить на вопрос"
    }

    fun editAnswerAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Edit answer"
        AppLanguage.RU -> "Изменить ответ"
    }

    fun closeComposerAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Done"
        AppLanguage.RU -> "Свернуть"
    }

    fun drawFilterAll(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Everyone"
        AppLanguage.RU -> "Все"
    }

    fun drawFilterMine(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "My Drawings"
        AppLanguage.RU -> "Мои"
    }

    fun drawFilterPartner(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Partner's"
        AppLanguage.RU -> "Партнёра"
    }

    fun drawNewCanvas(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "New Drawing"
        AppLanguage.RU -> "Новый рисунок"
    }

    fun drawSaveAsNew(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save Drawing"
        AppLanguage.RU -> "Сохранить рисунок"
    }

    fun drawDrawnOn(dateDisplay: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Drawn $dateDisplay"
        AppLanguage.RU -> "Нарисовано $dateDisplay"
    }

    fun tabTrips(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Trips"
        AppLanguage.RU -> "Поездки"
    }

    fun tripsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Past Meetups & Trips"
        AppLanguage.RU -> "Наши поездки и встречи"
    }

    fun tripsSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save trip stories and photos auto-sorted by the day they were taken"
        AppLanguage.RU -> "Храните воспоминания о встречах и фото, отсортированные по дате съёмки"
    }

    fun addTripButton(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add Trip"
        AppLanguage.RU -> "Добавить поездку"
    }

    fun editTripButton(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Edit Trip"
        AppLanguage.RU -> "Изменить поездку"
    }

    fun tripsViewLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Trips"
        AppLanguage.RU -> "Поездки"
    }

    fun allPhotosByDateLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Photos by Date"
        AppLanguage.RU -> "Фото по датам"
    }

    fun tripTitleLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Trip or Meetup Name"
        AppLanguage.RU -> "Название поездки или встречи"
    }

    fun tripLocationLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "City or Place"
        AppLanguage.RU -> "Город или место"
    }

    fun tripStartDateLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Start Date"
        AppLanguage.RU -> "Дата начала"
    }

    fun tripEndDateLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "End Date"
        AppLanguage.RU -> "Дата окончания"
    }

    fun tripNotesLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "What We Did & Memories"
        AppLanguage.RU -> "Что мы делали и тёплые детали"
    }

    fun tripNotesHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Where we walked, favorite food, funny moments..."
        AppLanguage.RU -> "Где гуляли, что ели, смешные моменты..."
    }

    fun addPhotosButton(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add Photos"
        AppLanguage.RU -> "Добавить фото"
    }

    fun noTripsYet(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "No past trips yet—add your favorite meetup and attach photos sorted by date!"
        AppLanguage.RU -> "Пока нет сохранённых поездок — добавьте встречу и загрузите фото по датам!"
    }

    fun noTripPhotosYet(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "No pictures added yet. Tap Add Photos to categorize them automatically by capture date."
        AppLanguage.RU -> "Фотографий пока нет. Нажмите «Добавить фото», чтобы разложить их по дате съёмки."
    }

    fun photosCount(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> if (count == 1) "1 photo" else "$count photos"
        AppLanguage.RU -> "$count фото"
    }

    fun photoCaptionHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Caption for this moment..."
        AppLanguage.RU -> "Подпись к этому моменту..."
    }

    fun changePhotoDate(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Change Date"
        AppLanguage.RU -> "Изменить дату"
    }

    fun tripSavedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Trip memory saved"
        AppLanguage.RU -> "Поездка сохранена"
    }

    fun photosAddedSuccess(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> if (count == 1) "1 photo added and sorted by date" else "$count photos added and sorted by date"
        AppLanguage.RU -> "Добавлено фото ($count) и разложено по датам"
    }

    fun coupleAvatarsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Couple Avatars"
        AppLanguage.RU -> "Аватары пары"
    }

    fun coupleAvatarsSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Tap your card to customize your name or avatar"
        AppLanguage.RU -> "Нажмите на свою карточку, чтобы изменить имя и аватар"
    }

    fun yourAvatarLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "You"
        AppLanguage.RU -> "Вы"
    }

    fun chooseAvatarFor(name: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> if (name.isBlank()) "Your Name & Avatar" else "Profile: $name"
        AppLanguage.RU -> if (name.isBlank()) "Ваше имя и аватар" else "Профиль: $name"
    }

    fun changeAvatarAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Edit name & avatar"
        AppLanguage.RU -> "Имя и аватар"
    }

    fun avatarUpdatedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Name & avatar updated"
        AppLanguage.RU -> "Имя и аватар обновлены"
    }

    fun welcomeSetupTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Welcome to Duet"
        AppLanguage.RU -> "Добро пожаловать в Дуэт"
    }

    fun welcomeSetupSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Create the name your partner sees & choose your avatar"
        AppLanguage.RU -> "Укажите имя для вашей пары и выберите аватар"
    }

    fun yourNameFieldLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Your Name"
        AppLanguage.RU -> "Ваше имя"
    }

    fun getStartedAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save & Continue"
        AppLanguage.RU -> "Сохранить и продолжить"
    }

    fun yourAnswerSection(name: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "$name (You)"
        AppLanguage.RU -> "$name (Вы)"
    }

    fun partnerAnswerSection(name: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "${name.ifBlank { "Partner" }}'s Answer"
        AppLanguage.RU -> "Ответ: ${name.ifBlank { "Партнёр" }}"
    }

    fun partnerOnlyAnswerHint(partnerName: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Only ${partnerName.ifBlank { "your partner" }} can write or edit their answer from their own account."
        AppLanguage.RU -> "Только ${partnerName.ifBlank { "партнёр" }} может писать и изменять свой ответ со своего аккаунта."
    }

    fun partnerInviteJoinHint(partnerName: String, inviteCode: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Share invite code $inviteCode in Couple Space so ${partnerName.ifBlank { "your partner" }} can write their own answer."
        AppLanguage.RU -> "Поделитесь кодом $inviteCode во вкладке «Пара», чтобы ${partnerName.ifBlank { "партнёр" }} писал(а) свои ответы сам(а)."
    }

    fun partnerReadOnlyBadge(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Partner only"
        AppLanguage.RU -> "Только партнёр"
    }

    fun chooseEmojiTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Choose Reaction Emoji"
        AppLanguage.RU -> "Выберите эмодзи"
    }

    fun customEmojiLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Type any emoji from keyboard"
        AppLanguage.RU -> "Или введите любой эмодзи с клавиатуры"
    }

    fun customEmojiHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "e.g. 🫶 or 🌻"
        AppLanguage.RU -> "напр. 🫶 или 🌻"
    }

    fun useEmojiAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Use"
        AppLanguage.RU -> "Выбрать"
    }

    fun clearReactionAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Remove"
        AppLanguage.RU -> "Убрать"
    }

    fun moreEmojisDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Choose any emoji reaction"
        AppLanguage.RU -> "Выбрать любой эмодзи"
    }

    fun aestheticSectionTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Couple Aesthetic & Theme"
        AppLanguage.RU -> "Стиль и палитра пары"
    }

    fun aestheticSectionSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Pick a shared palette or custom color — synced across both devices"
        AppLanguage.RU -> "Выберите общую палитру или свой цвет — синхронизируется на обоих телефонах"
    }

    fun aestheticCustomizeAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Customize"
        AppLanguage.RU -> "Выбрать"
    }

    fun aestheticDialogTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Choose Couple Aesthetic"
        AppLanguage.RU -> "Палитра вашего пространства"
    }

    fun aestheticPresetsHeading(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "CURATED AESTHETICS"
        AppLanguage.RU -> "ГОТОВЫЕ ПАЛИТРЫ"
    }

    fun aestheticCustomHeading(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "CUSTOM COUPLE COLOR"
        AppLanguage.RU -> "СВОЙ ОТТЕНОК ПАРЫ"
    }

    fun aestheticApplyCustom(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Apply Custom Color"
        AppLanguage.RU -> "Применить свой цвет"
    }

    fun aestheticCustomName(hex: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Custom Color ($hex)"
        AppLanguage.RU -> "Свой оттенок ($hex)"
    }

    fun aestheticUpdatedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Couple aesthetic synced across both devices"
        AppLanguage.RU -> "Палитра пары обновлена на обоих устройствах"
    }

    fun moreEmojisOption(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "More emojis..."
        AppLanguage.RU -> "Другие эмодзи..."
    }

    fun relationshipEyebrow(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "TOGETHER SINCE"
        AppLanguage.RU -> "ВМЕСТЕ С"
    }

    fun relationshipEmptyTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "When did you start dating?"
        AppLanguage.RU -> "Когда вы начали встречаться?"
    }

    fun relationshipEmptySubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add your start date to track how long you've been together & your next anniversary."
        AppLanguage.RU -> "Добавьте дату, чтобы считать дни вместе и видеть следующую годовщину."
    }

    fun relationshipStartedOn(dateDisplay: String, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Started $dateDisplay"
        AppLanguage.RU -> "Начало: $dateDisplay"
    }

    fun relationshipTotalDays(days: Long, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> if (days == 1L) "1 day together" else "%,d days together".format(java.util.Locale.US, days)
        AppLanguage.RU -> {
            val mod100 = (days % 100L).toInt()
            val mod10 = (days % 10L).toInt()
            val word = when {
                mod100 in 11..14 -> "дней"
                mod10 == 1 -> "день"
                mod10 in 2..4 -> "дня"
                else -> "дней"
            }
            "$days $word вместе"
        }
    }

    fun relationshipDurationHeadline(
        years: Int,
        months: Int,
        days: Int,
        totalDays: Long,
        lang: AppLanguage
    ): String {
        if (totalDays == 0L) {
            return if (lang == AppLanguage.RU) "Сегодня первый день!" else "Day 1 begins today!"
        }
        val parts = mutableListOf<String>()
        if (years > 0) {
            parts += if (lang == AppLanguage.RU) {
                val mod100 = years % 100
                val mod10 = years % 10
                val unit = when {
                    mod100 in 11..14 -> "лет"
                    mod10 == 1 -> "год"
                    mod10 in 2..4 -> "года"
                    else -> "лет"
                }
                "$years $unit"
            } else {
                if (years == 1) "1 yr" else "$years yrs"
            }
        }
        if (months > 0) {
            parts += if (lang == AppLanguage.RU) "$months мес." else if (months == 1) "1 mo" else "$months mos"
        }
        if (days > 0 || parts.isEmpty()) {
            parts += if (lang == AppLanguage.RU) "$days дн." else if (days == 1) "1 day" else "$days days"
        }
        return parts.joinToString(" • ")
    }

    fun anniversaryOrdinal(number: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> {
            val suffix = when {
                number % 100 in 11..13 -> "th"
                number % 10 == 1 -> "st"
                number % 10 == 2 -> "nd"
                number % 10 == 3 -> "rd"
                else -> "th"
            }
            "$number$suffix Anniversary"
        }
        AppLanguage.RU -> "$number-я годовщина"
    }

    fun nextAnniversaryTitle(number: Int, isToday: Boolean, lang: AppLanguage): String {
        val ordinal = anniversaryOrdinal(number, lang)
        return if (isToday) {
            if (lang == AppLanguage.RU) "С праздником! $ordinal" else "Happy $ordinal!"
        } else {
            if (lang == AppLanguage.RU) "Следующая: $ordinal" else "Next: $ordinal"
        }
    }

    fun relationshipDialogTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Date You Started Dating"
        AppLanguage.RU -> "Дата начала отношений"
    }

    fun saveDateAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Save Date"
        AppLanguage.RU -> "Сохранить дату"
    }

    fun clearDateAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Clear"
        AppLanguage.RU -> "Сбросить"
    }

    fun relationshipDateSaved(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Relationship start date updated"
        AppLanguage.RU -> "Дата отношений обновлена"
    }

    fun widgetSectionTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Home Screen Widget"
        AppLanguage.RU -> "Виджет на экран"
    }

    fun widgetSectionSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "See your days together and next reunion countdown right on your home screen"
        AppLanguage.RU -> "Счётчик дней вместе и отсчёт до следующей встречи прямо на главном экране"
    }

    fun addWidgetToHomeButton(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add to Home Screen"
        AppLanguage.RU -> "Добавить на экран"
    }

    fun widgetPinPromptSent(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Confirm the prompt to place the Duet widget on your home screen"
        AppLanguage.RU -> "Подтвердите добавление виджета Дуэт на главный экран"
    }

    fun widgetPinManualTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Add Duet Widget"
        AppLanguage.RU -> "Как добавить виджет"
    }

    fun widgetPinManualBody(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Long-press an empty space on your home screen, open Widgets, and drag \"Days & Reunion\" under Duet onto your screen."
        AppLanguage.RU -> "Зажмите пустое место на рабочем столе, откройте «Виджеты» и перетащите «Дни и встреча» из Дуэта на экран."
    }

    fun gotItAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Got it"
        AppLanguage.RU -> "Понятно"
    }

    fun selectedPhotosCount(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "$count selected"
        AppLanguage.RU -> "Выбрано: $count"
    }

    fun selectAllAction(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "All"
        AppLanguage.RU -> "Все"
    }

    fun deleteSelectedPhotosTitle(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> if (count == 1) "Delete 1 photo?" else "Delete $count photos?"
        AppLanguage.RU -> "Удалить фото ($count)?"
    }

    fun deleteSelectedPhotosBody(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Selected pictures will be removed from this trip on both devices."
        AppLanguage.RU -> "Выбранные фотографии будут удалены из воспоминаний о поездке на обоих устройствах."
    }

    fun photosDeletedSuccess(count: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> if (count == 1) "1 photo deleted" else "$count photos deleted"
        AppLanguage.RU -> "Удалено фото: $count"
    }
}
