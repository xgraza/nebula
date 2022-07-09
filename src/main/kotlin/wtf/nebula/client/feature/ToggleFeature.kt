package wtf.nebula.client.feature

import wtf.nebula.client.Nebula

open class ToggleFeature : Feature() {
    var toggled = false
        set(value) {
            field = value
            if (value) {
                Nebula.BUS.subscribe(this)
                onActivated()
            }

            else {
                Nebula.BUS.unsubscribe(this)
                onDeactivated()
            }
        }

    protected open fun onActivated() {

    }

    protected open fun onDeactivated() {

    }
}