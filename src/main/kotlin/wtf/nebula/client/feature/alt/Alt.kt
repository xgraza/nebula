package wtf.nebula.client.feature.alt

import com.mojang.authlib.Agent
import com.mojang.authlib.exceptions.AuthenticationException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator
import net.minecraft.util.Session
import wtf.nebula.util.Globals
import java.net.Proxy

class Alt(val email: String, val password: String, val altType: AltType) : Globals {
    fun login() {
        when (altType) {
            AltType.CRACKED ->
                mc.session = Session(email, "", "", "mojang")

            AltType.MOJANG -> {
                val service = YggdrasilAuthenticationService(Proxy.NO_PROXY, "")
                val auth = service.createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication

                auth.setUsername(email)
                auth.setPassword(password)

                try {
                    auth.logIn()
                    val profile = auth.selectedProfile
                    mc.session = Session(profile.name, profile.id.toString(), auth.authenticatedToken, "mojang")
                } catch (e: AuthenticationException) {
                    e.printStackTrace()
                }
            }

            AltType.MICROSOFT -> {
                val authenticator = MicrosoftAuthenticator()
                try {
                    val result = authenticator.loginWithCredentials(email, password)
                    val profile = result.profile
                    mc.session = Session(profile.name, profile.id, result.accessToken, "mojang")
                } catch (e: MicrosoftAuthenticationException) {
                    e.printStackTrace()
                }
            }
        }
    }
}