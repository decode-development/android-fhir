package com.google.android.fhir.p2p

import ca.uhn.fhir.context.FhirContext
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ResourceType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class SyncPayloadTest {
  @Test
  fun encodeToStringEncodesThePayloadToAJsonString() {
    val resourceType = "Patient"
    val resourceId = "abc"
    val resource = Patient().apply { this.id = resourceId }
    val encodedResource = FhirContext.forR4().newJsonParser().encodeResourceToString(resource)
    val patch = "[{\"op\":\"add\"}]"
    val createTimestamp = "2021-11-03"
    val updateTimestamp = "2021-11-04"
    val deleteTimestamp = "2021-11-05"

    val encoded = Json.encodeToString(
      SyncPayload(
        resource,
        listOf(
          RemoteChange.Create(createTimestamp, encodedResource),
          RemoteChange.Update(updateTimestamp, resourceType, resourceId, patch),
          RemoteChange.Delete(deleteTimestamp, resourceType, resourceId),
        )
      )
    )

    assertThat(encoded).isEqualTo("""{"resource":${Json.encodeToString(encodedResource)},"changes":[{"type":"create","timestamp":"$createTimestamp","resource":${Json.encodeToString(encodedResource)}},{"type":"update","timestamp":"$updateTimestamp","resourceType":"$resourceType","resourceId":"$resourceId","patch":${Json.encodeToString(patch)}},{"type":"delete","timestamp":"$deleteTimestamp","resourceType":"$resourceType","resourceId":"$resourceId"}]}""")
  }
}
