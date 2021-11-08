package com.google.android.fhir.p2p

import ca.uhn.fhir.context.FhirContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.hl7.fhir.instance.model.api.IBaseResource

/**
 * The payload for syncing changes between peers.
 */
@Serializable
data class SyncPayload(
  /**
   * The current version of the resource after all of the changes have been applied.
   */
  @Serializable(with = FhirResourceSerializer::class)
  val resource: IBaseResource,
  /**
   * The list of changes which have been made to the resource since it was last updated from the server.
   */
  val changes: List<RemoteChange>,
)

/**
 * A change to a resource which was made by another client.
 */
@Serializable
sealed class RemoteChange {
  /**
   * The timestamp at which the change took place.
   */
  abstract val timestamp: String

  /**
   * The creation of a new resource locally on the client.
   */
  @Serializable
  @SerialName("create")
  data class Create(override val timestamp: String, val resource: String): RemoteChange()

  /**
   * An update to a resource.
   */
  @Serializable
  @SerialName("update")
  data class Update(override val timestamp: String, val resourceType: String, val resourceId: String, val patch: String): RemoteChange()

  /**
   * The deletion of a resource.
   */
  @Serializable
  @SerialName("delete")
  data class Delete(override val timestamp: String, val resourceType: String, val resourceId: String): RemoteChange()
}

private class FhirResourceSerializer: KSerializer<IBaseResource> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(IBaseResource::class.qualifiedName!!, PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: IBaseResource) = encoder.encodeString(FhirContext.forR4().newJsonParser().encodeResourceToString(value))
  override fun deserialize(decoder: Decoder): IBaseResource = FhirContext.forR4().newJsonParser().parseResource(decoder.decodeString())
}
