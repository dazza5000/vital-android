package io.tryvital.vitaldevices

import java.util.*

const val VITAL_BLE_SIMULATOR = "_vital_ble_simulator_"

fun devices(): List<DeviceModel> {
    return listOf(
        DeviceModel(
            id = "omron_m4",
            name = "Omron Intelli IT M4",
            brand = Brand.Omron,
            kind = Kind.BloodPressure
        ),
        DeviceModel(
            id = "omron_m7",
            name = "Omron Intelli IT M7",
            brand = Brand.Omron,
            kind = Kind.BloodPressure
        ),
        DeviceModel(
            id = "accuchek_guide",
            name = "Accu-Chek Guide",
            brand = Brand.AccuChek,
            kind = Kind.GlucoseMeter
        ),
        DeviceModel(
            id = "accuchek_guide_me",
            name = "Accu-Chek Guide Me",
            brand = Brand.AccuChek,
            kind = Kind.GlucoseMeter
        ),
        DeviceModel(
            id = "accuchek_guide_active",
            name = "Accu-Chek Active",
            brand = Brand.AccuChek,
            kind = Kind.GlucoseMeter
        ),
        DeviceModel(
            id = VITAL_BLE_SIMULATOR,
            name = "Vital BLE Simulator",
            brand = Brand.AccuChek,
            kind = Kind.GlucoseMeter
        ),
        DeviceModel(
            id = "contour_next_one",
            name = "Contour Next One",
            brand = Brand.Contour,
            kind = Kind.GlucoseMeter
        ),
        DeviceModel(
            id = "beurer",
            name = "Beurer Devices",
            brand = Brand.Beurer,
            kind = Kind.BloodPressure
        ),
        DeviceModel(
            id = "libre1",
            name = "Freestyle Libre 1",
            brand = Brand.Libre,
            kind = Kind.GlucoseMeter
        )
    )
}

fun devices(brand: Brand): List<DeviceModel> {
    return devices().filter { it.brand == brand }
}

fun codes(deviceId: String): List<String> {
    when (deviceId) {
        "omron_m4" -> return listOf("OMRON", "M4", "X4", "BLESMART")
        "omron_m7" -> return listOf("OMRON", "M7", "BLESMART")
        "accuchek_guide",
        "accuchek_guide_active",
        "accuchek_guide_me" -> return listOf("meter")
        "contour_next_one" -> return listOf("Ocontour")
        "beurer" -> return listOf("Beuerer", "BC", "bc")
        VITAL_BLE_SIMULATOR -> return listOf(VITAL_BLE_SIMULATOR)
    }

    return emptyList()
}

fun serviceUUID(kind: Kind): UUID {
    return when (kind) {
        Kind.GlucoseMeter -> UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")
        Kind.BloodPressure -> UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
    }
}