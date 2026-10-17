package org.lsposed.corepatch.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.lsposed.corepatch.Config
import org.lsposed.corepatch.R
import org.lsposed.corepatch.data.SwitchData
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** All module switches in one card, moved over from the old single-page UI. */
@Composable
fun FeaturesScreen(
    serviceAvailable: Boolean,
    isBlurEnabled: Boolean,
    extraBottomPadding: Dp = 0.dp,
) {
    if (!serviceAvailable) {
        ServiceUnavailable(isBlurEnabled = isBlurEnabled, extraBottomPadding = extraBottomPadding)
        return
    }
    val switches = switchItems()
    val checkedStates = remember { mutableStateMapOf<String, Boolean>() }
    var warningText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        switches.forEach { checkedStates[it.key] = Config.getConfig(it.key) }
    }
    PageScaffold(
        title = stringResource(R.string.tab_features),
        isBlurEnabled = isBlurEnabled,
        extraBottomPadding = extraBottomPadding,
    ) {
        item {
            Card(modifier = Modifier.firstCard()) {
                switches.forEach { item ->
                    SwitchPreference(
                        title = item.title,
                        summary = item.description,
                        checked = checkedStates[item.key] ?: false,
                        onCheckedChange = { checked ->
                            Config.setConfig(item.key, checked)
                            checkedStates[item.key] = checked
                            if (checked && item.warning != null) {
                                warningText = item.warning
                            }
                        },
                    )
                }
            }
        }
    }
    OverlayDialog(
        show = warningText != null,
        summary = warningText,
        onDismissRequest = { warningText = null },
        content = {
            TextButton(
                text = stringResource(android.R.string.ok),
                onClick = { warningText = null },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        },
    )
}

@Composable
private fun ServiceUnavailable(isBlurEnabled: Boolean, extraBottomPadding: Dp) {
    PageScaffold(
        title = stringResource(R.string.tab_features),
        isBlurEnabled = isBlurEnabled,
        extraBottomPadding = extraBottomPadding,
    ) {
        item {
            Column(
                modifier = Modifier.padding(top = 120.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.xposed_service_unavailable),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.title4,
                )
                Text(
                    text = stringResource(R.string.xposed_service_unavailable_summary),
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.body2,
                )
            }
        }
    }
}

@Composable
private fun switchItems(): List<SwitchData> {
    val usePreviousSignaturesWarning = if (isMiui()) {
        stringResource(R.string.miui_usepresig_warn) + "\n\n" + stringResource(R.string.use_previous_signatures_warning)
    } else {
        stringResource(R.string.use_previous_signatures_warning)
    }
    return listOf(
        SwitchData(
            stringResource(R.string.bypass_downgrade),
            stringResource(R.string.bypass_downgrade_summary),
            Config.BYPASS_DOWNGRADE
        ),
        SwitchData(
            stringResource(R.string.bypass_min_sdk),
            stringResource(R.string.bypass_min_sdk_summary),
            Config.BYPASS_MIN_SDK
        ),
        SwitchData(
            stringResource(R.string.bypass_verification),
            stringResource(R.string.bypass_verification_summary),
            Config.BYPASS_VERIFICATION
        ),
        SwitchData(
            stringResource(R.string.bypass_resource_arsc_restrictions),
            stringResource(R.string.bypass_resource_arsc_restrictions_summary),
            Config.BYPASS_RESOURCE_ARSC_RESTRICTIONS
        ),
        SwitchData(
            stringResource(R.string.bypass_digest),
            stringResource(R.string.bypass_digest_summary),
            Config.BYPASS_DIGEST
        ),
        SwitchData(
            stringResource(R.string.bypass_exact_signature_match),
            stringResource(R.string.bypass_exact_signature_match_summary),
            Config.BYPASS_EXACT_SIGNATURE_MATCH
        ),
        SwitchData(
            stringResource(R.string.use_previous_signatures),
            stringResource(R.string.use_previous_signatures_summary),
            Config.USE_PREVIOUS_SIGNATURES,
            usePreviousSignaturesWarning
        ),
        SwitchData(
            stringResource(R.string.allow_hidden_apis_for_system_apps),
            stringResource(R.string.allow_hidden_apis_for_system_apps_summary),
            Config.ALLOW_HIDDEN_APIS_FOR_SYSTEM_APPS
        ),
        SwitchData(
            stringResource(R.string.bypass_shared_user),
            stringResource(R.string.bypass_shared_user_summary),
            Config.BYPASS_SHARED_USER
        ),
        SwitchData(
            stringResource(R.string.disable_verification_agent),
            stringResource(R.string.disable_verification_agent_summary),
            Config.DISABLE_VERIFICATION_AGENT
        ),
        SwitchData(
            stringResource(R.string.bypass_block),
            stringResource(R.string.bypass_block_summary),
            Config.BYPASS_BLOCK
        ),
    )
}

@SuppressLint("PrivateApi")
private fun isMiui(): Boolean = try {
    val systemProperties = Class.forName("android.os.SystemProperties")
    val get = systemProperties.getMethod("get", String::class.java)
    (get.invoke(null, "ro.miui.ui.version.code") as String).isNotEmpty()
} catch (_: ReflectiveOperationException) {
    false
}
