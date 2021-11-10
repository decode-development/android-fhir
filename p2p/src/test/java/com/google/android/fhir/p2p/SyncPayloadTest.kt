/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.p2p

import ca.uhn.fhir.context.FhirContext
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hl7.fhir.r4.model.Patient
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class SyncPayloadTest {
  @Test
  fun encodeToStringEncodesThePayloadToAJsonString() {
    val resourceType = "Patient"
    val resourceId = "abc"
    val resource = Patient().apply { id = resourceId }
    val encodedResource = FhirContext.forR4().newJsonParser().encodeResourceToString(resource)
    val patch = "[{\"op\":\"add\"}]"
    val createTimestamp = "2021-11-03"
    val updateTimestamp = "2021-11-04"
    val deleteTimestamp = "2021-11-05"

    val encoded =
      Json.encodeToString(
        SyncPayload(
          resource,
          listOf(
            RemoteChange.Create(createTimestamp, encodedResource),
            RemoteChange.Update(updateTimestamp, resourceType, resourceId, patch),
            RemoteChange.Delete(deleteTimestamp, resourceType, resourceId),
          )
        )
      )

    assertThat(encoded)
      .isEqualTo(
        """{"resource":${Json.encodeToString(encodedResource)},"changes":[{"type":"create","timestamp":${Json.encodeToString(createTimestamp)},"resource":${Json.encodeToString(encodedResource)}},{"type":"update","timestamp":${Json.encodeToString(updateTimestamp)},"resourceType":${Json.encodeToString(resourceType)},"resourceId":${Json.encodeToString(resourceId)},"patch":${Json.encodeToString(patch)}},{"type":"delete","timestamp":${Json.encodeToString(deleteTimestamp)},"resourceType":${Json.encodeToString(resourceType)},"resourceId":${Json.encodeToString(resourceId)}}]}"""
      )
  }

  @Test
  fun decodeFromStringDecodesThePayloadFromAJsonString() {
    val resourceType = "Patient"
    val resourceId = "asdf"
    val resource = Patient().apply { id = resourceId }
    val encodedResource = FhirContext.forR4().newJsonParser().encodeResourceToString(resource)
    val patch = "[{\"op\":\"add\"}]"
    val createTimestamp = "2021-11-03"
    val updateTimestamp = "2021-11-04"
    val deleteTimestamp = "2021-11-05"
    val payload =
      """{"resource":${Json.encodeToString(encodedResource)},"changes":[{"type":"create","timestamp":${Json.encodeToString(createTimestamp)},"resource":${Json.encodeToString(encodedResource)}},{"type":"update","timestamp":${Json.encodeToString(updateTimestamp)},"resourceType":${Json.encodeToString(resourceType)},"resourceId":${Json.encodeToString(resourceId)},"patch":${Json.encodeToString(patch)}},{"type":"delete","timestamp":${Json.encodeToString(deleteTimestamp)},"resourceType":${Json.encodeToString(resourceType)},"resourceId":${Json.encodeToString(resourceId)}}]}"""

    val decoded: SyncPayload = Json.decodeFromString(payload)

    assertThat(decoded.resource).isInstanceOf(Patient::class.java)
    assertThat(decoded.resource.idElement.idPart).isEqualTo(resourceId)
    assertThat(decoded.changes)
      .isEqualTo(
        listOf(
          RemoteChange.Create(createTimestamp, encodedResource),
          RemoteChange.Update(updateTimestamp, resourceType, resourceId, patch),
          RemoteChange.Delete(deleteTimestamp, resourceType, resourceId),
        )
      )
  }
}
