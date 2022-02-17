package com.google.android.fhir.p2p

interface ConflictResolutionStrategy {

  fun resolveConflict(theirs: SyncPayload, ours: SyncPayload): SyncPayload
}
