package jp.co.studio.kaka.ui.settings

import jp.co.studio.kaka.domain.model.AppThemeId
import jp.co.studio.kaka.domain.model.ThemeMode
import jp.co.studio.kaka.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeSettingsRepository(mode: ThemeMode, theme: AppThemeId) : SettingsRepository {
        private val modeFlow = MutableStateFlow(mode)
        private val themeFlow = MutableStateFlow(theme)
        override val themeMode: Flow<ThemeMode> = modeFlow
        override val themeId: Flow<AppThemeId> = themeFlow
        override suspend fun setThemeMode(mode: ThemeMode) { modeFlow.value = mode }
        override suspend fun setThemeId(id: AppThemeId) { themeFlow.value = id }
        val mode get() = modeFlow.value
        val theme get() = themeFlow.value
    }

    @Test
    fun `selecting a theme while following the system keeps following the system`() = runTest(dispatcher) {
        val repository = FakeSettingsRepository(ThemeMode.SYSTEM, AppThemeId.NIGHT)
        val viewModel = SettingsViewModel(repository)

        viewModel.selectTheme(AppThemeId.MINT)
        advanceUntilIdle()

        assertEquals(AppThemeId.MINT, repository.theme)
        assertEquals(ThemeMode.SYSTEM, repository.mode)
    }

    @Test
    fun `selecting a dark theme while forced light switches the mode to dark so the change is visible`() = runTest(dispatcher) {
        val repository = FakeSettingsRepository(ThemeMode.LIGHT, AppThemeId.MOONLIGHT)
        val viewModel = SettingsViewModel(repository)

        viewModel.selectTheme(AppThemeId.ONYX)
        advanceUntilIdle()

        assertEquals(AppThemeId.ONYX, repository.theme)
        assertEquals(ThemeMode.DARK, repository.mode)
    }

    @Test
    fun `selecting a light theme while forced dark switches the mode to light`() = runTest(dispatcher) {
        val repository = FakeSettingsRepository(ThemeMode.DARK, AppThemeId.NIGHT)
        val viewModel = SettingsViewModel(repository)

        viewModel.selectTheme(AppThemeId.SAKURA)
        advanceUntilIdle()

        assertEquals(AppThemeId.SAKURA, repository.theme)
        assertEquals(ThemeMode.LIGHT, repository.mode)
    }

    @Test
    fun `setThemeMode is passed straight through`() = runTest(dispatcher) {
        val repository = FakeSettingsRepository(ThemeMode.SYSTEM, AppThemeId.NIGHT)
        val viewModel = SettingsViewModel(repository)

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, repository.mode)
        assertEquals(AppThemeId.NIGHT, repository.theme)
    }
}
