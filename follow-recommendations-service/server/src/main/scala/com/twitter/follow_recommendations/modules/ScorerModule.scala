package com.twitter.follow_recommendations.modules

import com.google.inject.Provides
import com.google.inject.Singleton
import com.twitter.inject.TwitterModule
import com.twitter.relevance.ep_model.common.CommonConstants
import com.twitter.relevance.ep_model.scorer.EPScorer
import com.twitter.relevance.ep_model.scorer.EPScorerBuilder
import java.io.File
import java.io.FileOutputStream
import scala.language.postfixOps

object ScorerModule extends TwitterModule {
  private val STPScorerPath = "/quality/stp_models/20141223"

  private def fileFromResource(resource: String): File = {
    // Restrict to classpath resources under the expected base directory to avoid path traversal.
    if (resource == null || !resource.startsWith(STPScorerPath + "/") || resource.contains("..")) {
      throw new IllegalArgumentException("Invalid resource path")
    }

    val inputStream = getClass.getResourceAsStream(resource)
    if (inputStream == null) {
      throw new IllegalArgumentException(s"Resource not found: $resource")
    }

    val file = File.createTempFile("stp_model_", ".tmp")
    file.deleteOnExit()

    val fos = new FileOutputStream(file)
    try {
      Iterator
        .continually(inputStream.read)
        .takeWhile(-1 !=)
        .foreach(fos.write)
    } finally {
      try fos.close() finally inputStream.close()
    }
    file
  }

  @Provides
  @Singleton
  def provideEpScorer: EPScorer = {
    val modelPath =
      fileFromResource(STPScorerPath + "/" + CommonConstants.EP_MODEL_FILE_NAME).getAbsolutePath
    val trainingConfigPath =
      fileFromResource(STPScorerPath + "/" + CommonConstants.TRAINING_CONFIG).getAbsolutePath
    val epScorer = new EPScorerBuilder
    epScorer
      .withModelPath(modelPath)
      .withTrainingConfig(trainingConfigPath)
      .build()
  }
}
