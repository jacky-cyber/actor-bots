package im.actor.bots.translate

import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.jdom2.input.DOMBuilder
import org.json.JSONObject
import java.net.URLEncoder
import javax.xml.parsers.DocumentBuilderFactory

val clientId = ""
val clientSecret = ""

var token: String? = null
var tokenIssued: Long? = null

/**
 * Translate phrase to English
 */
fun translate(source: String): String {

    if (token == null || (System.currentTimeMillis() - tokenIssued!!) > 5 * 60 * 1000L) {
        token = registerToken(clientId, clientSecret)
        tokenIssued = System.currentTimeMillis()
    }

    // Building request
    val urlBuilder = URIBuilder("http://api.microsofttranslator.com/v2/Http.svc/Translate")
    urlBuilder.addParameter("text", source)
    urlBuilder.addParameter("to", "en")
    val url = urlBuilder.build()

    // Getting request
    val client = HttpClients.createDefault()
    val get = HttpGet(url)
    get.addHeader("Authorization", "Bearer $token")
    val response = client.execute(get).entity
    var responseString = EntityUtils.toString(response, "UTF-8");

    val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = DOMBuilder().build(docBuilder.parse(IOUtils.toInputStream(responseString)))

    return doc.rootElement.textNormalize
}

/**
 * Getting access token from Azure Marketplace
 */
fun registerToken(clientId: String, clientSecret: String): String {

    val client = HttpClients.createDefault()
    val post = HttpPost("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13")
    val cliendIdEncoded = URLEncoder.encode(clientId)
    val cliendSecretEncoded = URLEncoder.encode(clientSecret)
    post.entity = StringEntity("grant_type=client_credentials&client_id=$cliendIdEncoded&client_secret=$cliendSecretEncoded&scope=http://api.microsofttranslator.com", ContentType.APPLICATION_FORM_URLENCODED)

    val response = client.execute(post).entity
    var responseString = EntityUtils.toString(response, "UTF-8");
    System.out.println(responseString)
    return JSONObject(responseString).getString("access_token")
}