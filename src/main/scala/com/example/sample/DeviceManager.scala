package com.example.sample

class DeviceManager {

}

object DeviceManager {
  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}
