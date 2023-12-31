package ae.geekhome.panel.ui.welcome

import ae.geekhome.panel.coap.CoapService
import ae.geekhome.panel.navigation.RouteNavigator
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.Inet4Address
import java.net.Inet6Address
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.californium.elements.util.NetworkInterfacesUtil

@HiltViewModel
class WelcomeViewModel
@Inject
constructor(private val coapService: CoapService, routeNavigator: RouteNavigator) :
    ViewModel(), CoapService.ServerStateChangedListener, RouteNavigator by routeNavigator {
    val ip4Address: Inet4Address = NetworkInterfacesUtil.getMulticastInterfaceIpv4()
    val ip6Address: Inet6Address = NetworkInterfacesUtil.getMulticastInterfaceIpv6()
    val state = mutableStateOf(coapService.state)
    val port = coapService.port

    init {
        coapService.stateListener = this
        state.value = coapService.state
        viewModelScope.launch { coapService.start() }
    }

    override fun onCleared() {
        viewModelScope.launch { coapService.stop() }
        coapService.stateListener = null
        super.onCleared()
    }

    override suspend fun onServerStateChanged(state: CoapService.ServerState) =
        withContext(Dispatchers.Main) { this@WelcomeViewModel.state.value = state }
}
