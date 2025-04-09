package io.openk9.datasource.model.projection;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.LargeLanguageModel;

public record BucketLargeLanguageModel(Bucket bucket, LargeLanguageModel largeLanguageModel) {
}
