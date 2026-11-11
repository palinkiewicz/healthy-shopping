package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.ui.designsystem.BackButton
import pl.dakil.healthyshopping.ui.designsystem.CollapsingScaffold
import pl.dakil.healthyshopping.ui.designsystem.FormSheet
import pl.dakil.healthyshopping.ui.designsystem.PageColumn
import pl.dakil.healthyshopping.ui.designsystem.SegmentedGroup
import pl.dakil.healthyshopping.ui.designsystem.ShapedIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.theme.Spacing
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@Composable
fun HistorySettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val limit by viewModel.recentlyViewedLimit.collectAsState()
    val items by viewModel.recentlyViewedItems.collectAsState()
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clearedMessage = stringResource(R.string.history_cleared)

    CollapsingScaffold(
        title = stringResource(R.string.settings_history),
        navigationIcon = { BackButton(onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PageColumn(padding, spacing = Spacing.xs) {
            SectionHeader(stringResource(R.string.settings_section_home))
            SegmentedGroup {
                SliderRow(
                    title = stringResource(R.string.settings_history_limit),
                    summary = if (limit == 0) stringResource(R.string.settings_history_limit_off)
                    else pluralStringResource(R.plurals.settings_history_limit_on, limit, limit),
                    value = limit,
                    onValueChange = viewModel::setRecentlyViewedLimit,
                    valueRange = 0..10,
                    steps = 9,
                )
            }

            SectionHeader(stringResource(R.string.settings_section_data))
            SegmentedGroup {
                SettingRow(
                    title = stringResource(R.string.settings_history_clear),
                    summary = if (items.isEmpty()) stringResource(R.string.settings_history_empty)
                    else pluralStringResource(R.plurals.settings_history_count, items.size, items.size),
                    enabled = items.isNotEmpty(),
                    onClick = { confirmClear = true },
                    leading = {
                        ShapedIcon(
                            AppSymbols.DeleteSweep,
                            polygon = MaterialShapes.Cookie9Sided,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    },
                )
            }
        }
    }

    if (confirmClear) {
        FormSheet(
            title = stringResource(R.string.history_clear_title),
            symbol = AppSymbols.DeleteSweep,
            supportingText = stringResource(R.string.history_clear_body),
            onDismiss = { confirmClear = false },
            dismissLabel = stringResource(R.string.action_cancel),
            confirmLabel = stringResource(R.string.action_clear),
            destructive = true,
            onConfirm = {
                viewModel.clearRecentlyViewed()
                confirmClear = false
                scope.launch { snackbarHostState.showSnackbar(clearedMessage) }
            },
        )
    }
}
