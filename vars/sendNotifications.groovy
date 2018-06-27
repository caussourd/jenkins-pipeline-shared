#!/usr/bin/env groovy

/**
 * Gather git commits
 */
def getChangeString() {
 MAX_MSG_LEN = 100
 def changeString = ""

 echo "Gathering SCM changes"
 def changeLogSets = currentBuild.changeSets
 for (int i = 0; i < changeLogSets.size(); i++) {
   def entries = changeLogSets[i].items
   for (int j = 0; j < entries.length; j++) {
     def entry = entries[j]
     truncated_msg = entry.msg.take(MAX_MSG_LEN)
     changeString += " - ${truncated_msg} [${entry.author}]\n"
   }
 }

 if (!changeString) {
   changeString = " - No new changes"
 }
   return changeString
}


/**
 * Send notifications based on build status string
 */
def call(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'ERROR'

  // Default values
  def color = 'danger'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def changes = getChangeString()
  def summary = "${subject} (<${env.BUILD_URL}|Open>)\n${changes}"
  def details = """<p>${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'good'
  } else if (buildStatus == 'SUCCESS') {
    color = 'good'
  }

  // Send notifications
  slackSend (color: color, message: summary)

}
