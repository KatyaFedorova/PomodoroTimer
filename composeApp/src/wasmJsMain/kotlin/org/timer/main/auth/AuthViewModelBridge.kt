package org.timer.main.auth

import com.diamondedge.logging.*
import kotlinx.browser.window
import org.koin.core.context.GlobalContext

fun registerMagicLinkHandler() {
    logging("registerMagicLinkHandler").d {"registerMagicLinkHandler"  }
    val vm = GlobalContext.get().get<AuthViewModel>()
    // проверяем текущий URL — если это magic link, обрабатываем сразу
    val href = window.location.href
    logging("registerMagicLinkHandler").d {"hrief = $href"  }
    val isMagicLink = href.contains("oobCode") && href.contains("mode=signIn")
   if (isMagicLink) {
        vm.onMagickLinkClick(href)
   }
}