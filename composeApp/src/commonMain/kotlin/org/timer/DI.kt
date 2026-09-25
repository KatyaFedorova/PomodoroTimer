package org.timer

import org.jetbrains.compose.resources.*
import org.koin.core.*
import org.koin.core.component.*
import org.koin.core.context.*
import org.koin.core.module.*
import org.koin.core.module.dsl.*
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.*
import org.koin.mp.*
import org.timer.main.auth.*
import org.timer.main.breakactivity.*
import org.timer.main.isMobile
import org.timer.main.domain.*
import org.timer.main.domain.auth.*
import org.timer.main.domain.exercise.*
import org.timer.main.domain.feedback.*
import org.timer.main.domain.game.*
import org.timer.main.domain.project.*
import org.timer.main.domain.settings.*
import org.timer.main.domain.statistic.*
import org.timer.main.domain.team.*
import org.timer.main.domain.timer.*
import org.timer.main.progress.header.*
import org.timer.main.projects.*
import org.timer.main.settings.*
import org.timer.main.statistic.*
import org.timer.main.timer.*
import org.timer.main.feedback.*
import org.timer.main.team.activity.*
@ExperimentalResourceApi
fun appModule() = module {
    //ViewModels
    viewModelOf(::SettingsViewModel)
    viewModelOf(::BreakActivityViewModel)
    // Explicit definition (not viewModelOf): the isMobile lambda has a default value, and
    // Koin only fills constructor defaults via reflection — which is unavailable on Wasm/Native,
    // so viewModelOf(::TimerViewModel) fails there. Passing isMobile literally avoids that.
    viewModel {
        TimerViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            isMobile = ::isMobile,
        )
    }
    viewModelOf(::CatBowlProgressViewModel)
    singleOf(::AuthViewModel)
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::StatisticViewModel)
    viewModelOf(::FeedbackViewModel)
    viewModelOf(::PlotViewModel)
    viewModelOf(::TeamActivityViewModel)

    //UseCases
    factoryOf(::GetProjectsUseCase)
    factoryOf(::AddProjectUseCase)
    factoryOf(::RemoveProjectUseCase)
    factoryOf(::AddTaskUseCase)
    factoryOf(::DoneTaskUseCase)
    factoryOf(::UndoneTaskUseCase)
    factoryOf(::UpdateProjectsOrderUseCase)
    factoryOf(::UpdateTasksOrderUseCase)
    factoryOf(::UpdateProjectNameUseCase)
    factoryOf(::UpdateTaskDescriptionUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::MoveTaskToTheEndOfListUseCase)
    factoryOf(::DefaultPlayAlarmUseCase)
    factoryOf(::DefaultCancelAlarmUseCase)
    factoryOf(::DeleteAllDoneTasksUseCase)
    factory<GetAuthStateUseCase> { DefaultGetAuthStateUseCase(get()) }
    factory<LoginWithGoogleUseCase> { DefaultLoginWithGoogleUseCase(get()) }
    factory<LogoutUseCase> { DefaultLogoutUseCase(get()) }
    factory<PlayAlarmUseCase> { DefaultPlayAlarmUseCase(get(), get()) }
    factory<CancelAlarmUseCase> { DefaultCancelAlarmUseCase(get()) }
    factory<LogoutUseCase> { DefaultLogoutUseCase(get()) }
    factory<GetCatBowlFillingProgressUseCase> { DefaultGetCatBowlFillingProgressUseCase(get()) }
    factory<IncrementCatBowlFillingProgressUseCase> { DefaultIncrementCatBowlFillingProgressUseCase(get(),get()) }
    factory<ResetCatBowlFillingProgressUseCase> { DefaultResetCatBowlFillingProgressUseCase(get(),get()) }
    factory<GetFeedingDatesUseCase> { DefaultGetFeedingDatesUseCase(get())}
    factory<AddFishWhenKittyFullUseCase> { DefaultAddFishWhenKittyFullUseCase(get()) }
    factory<GetFishesNumberUseCaseUseCase> { DefaultGetFishesNumberUseCase(get()) }
    factory<GetKittidorosNumberUseCase> { DefaultGetKittidorosNumberUseCase(get()) }
    factory<IncrementKittdorsNumberUseCase> { DefaultIncrementKittdorsNumberUseCase(get(),get(),get()) }
    factory<ResetKittidoroNumberUseCase> { DefaultResetKittidoroNumberUseCase(get(),get()) }
    factory<SendFeedbackUseCase> { DefaultSendFeedbackUseCase(get()) }
    factory<SaveEventUseCase> { DefaultSaveEventUseCase(get(),get()) }
    factory<SendMagicLinkUseCase> { DefaultSendMagicLinkUseCase(get()) }
    factory<LoginWithMagicLinkUseCase> { DefaultMagicLinkLoginUseCase(get()) }
    factory<GetTeamMembersWithStatusesUseCase> { DefaultGetTeamMembersStatusesUseCase(get(),get(),get(),get()) }
    factory<StopTimerUseCase> { DefaultStopTimerUseCase(get()) }
    factory<StartPomodoroUseCase> { StartPomodoroUseCase(get()) }
    factory<StartShortBreakUseCase> { StartShortBreakUseCase(get()) }
    factory<StartLongBreakUseCase> { StartLongBreakUseCase(get()) }
    factory<StartTimerUseCase> { DefaultStartTimerUseCase(get()) }
    factory<IncrementExerciseUseCase> { DefaultIncrementExerciseUseCase(get(),get()) }
    factory<ResetExercisesNumberUseCase> { DefaultResetExercisesNumberUseCase(get(),get()) }
    factory<GetWeeklyEventStatsUseCase> { DefaultGetWeeklyEventStatsUseCase(get(),get()) }
    factory<GetMonthlyEventStatsUseCase> { DefaultGetMonthlyEventStatsUseCase(get(),get()) }
    factory<GetWeeklyPomodoroStatsUseCase> { DefaultGetWeeklyPomodoroStatsUseCase(get()) }
    factory<GetMonthlyPomodoroStatsUseCase> { DefaultGetMonthlyPomodoroStatsUseCase(get()) }
    factory<GetWeeklyExerciseStatsUseCase> { DefaultGetWeeklyExerciseStatsUseCase(get()) }
    factory<GetMonthlyExerciseStatsUseCase> { DefaultGetMonthlyExerciseStatsUseCase(get()) }
    factory<GetWeeklyTaskStatsUseCase> { DefaultGetWeeklyTaskStatsUseCase(get()) }
    factory<GetMonthlyTaskStatsUseCase> { DefaultGetMonthlyTaskStatsUseCase(get()) }

    //infrastructure
    factoryOf<SystemClock>(::DefaultSystemClock)
    factory<TimerFactory> { DefaultTimerFactory() }

    //Gateways
    singleOf(::PersistentSettingsGateway)
    single<SettingsGateway> { FirebaseSettingsGateway() }
    single<ProjectsGateway> { FirebaseProjectsGateway() }
    single<AuthGateway> { DefaultAuthGateway() }
    single<GameGateway> { FirebaseGameGateway() }
    single<TimerGateway> { FirebaseTimerGateway() }
    single<FeedbackGateway> { FirebaseFeedbackGateway() }
    single<StatisticsEventGateway> { FirebaseStatisticsEventGateway() }
    single<TeamGateway> { FirebaseTeamGateway() }
    single<ExerciseGateway> { FirebaseExerciseGateway() }
}

expect fun platformSpecificModule(): Module

@OptIn(ExperimentalResourceApi::class)
fun initializeKoin(
    conf: (KoinApplication.() -> Unit)? = null
) {

    startKoin {
        conf?.invoke(this)
        modules(appModule(), platformSpecificModule())
    }
}

expect fun afterKoinStarted()

object IOSViewModelProvider : KoinComponent {
    val authViewModel by inject<AuthViewModel>()
}
