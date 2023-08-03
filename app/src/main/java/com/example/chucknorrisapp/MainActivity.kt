package com.example.chucknorrisapp
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var categorySpinner: Spinner
    private lateinit var btnFetchJoke: Button
    private lateinit var jokePopup: Dialog
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        storage = FirebaseStorage.getInstance()

        jokePopup = Dialog(this)
        jokePopup.setContentView(R.layout.joke_popup)
        jokePopup.setCancelable(true)

        categorySpinner = findViewById(R.id.spinnerCategory)
        btnFetchJoke = findViewById(R.id.btnFetchJoke)

        // Fetch categories and populate the spinner
        CoroutineScope(Dispatchers.IO).launch {
            val categories = fetchCategories()
            withContext(Dispatchers.Main) {
                populateSpinner(categories)
            }
        }

        btnFetchJoke.setOnClickListener {
            val selectedCategory = categorySpinner.selectedItem as String
            fetchJoke(selectedCategory)
        }
    }

    private fun fetchCategories(): List<String> {
        val urlString = "https://api.chucknorris.io/jokes/categories"
        var connection: HttpURLConnection? = null
        val categories = mutableListOf<String>()
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000 // 5 seconds timeout
            connection.readTimeout = 5000 // 5 seconds timeout

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                val jsonArray = JSONArray(stringBuilder.toString())
                for (i in 0 until jsonArray.length()) {
                    categories.add(jsonArray.getString(i))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return categories

    }

    private fun populateSpinner(categories: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun fetchJoke(category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val urlString = "https://api.chucknorris.io/jokes/random?category=$category"

            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000 // 5 seconds timeout
                connection.readTimeout = 5000 // 5 seconds timeout

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    val joke = parseJokeFromResponse(stringBuilder.toString())
                    withContext(Dispatchers.Main) {
                        // Show the joke in the popup
                        showJokePopup(joke)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error: ${connection.responseCode}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun parseJokeFromResponse(response: String): String {
        try {
            val jsonObject = JSONObject(response)
            return jsonObject.getString("value")
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error parsing data"
        }
    }

    private fun showJokePopup(joke: String) {
        val txtJokePopup: TextView = jokePopup.findViewById(R.id.txtJoke)
        txtJokePopup.text = joke

        val btnClosePopup: ImageButton = jokePopup.findViewById(R.id.btnExitPopup)
        btnClosePopup.setOnClickListener {
            jokePopup.dismiss()
        }

        fetchImage() // Fetch and display the image

        jokePopup.show()
    }

    private fun fetchImage() {
        val category = categorySpinner.selectedItem.toString()
        val imageFileName = "photo.jpg"
        val projectRef: StorageReference = storage.reference.child("$category")
        val imageRef: StorageReference = projectRef.child(imageFileName)

        val imageView: ImageView = jokePopup.findViewById(R.id.imgJoke)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            // Use any image loading library or method to load the image into the ImageView
            // Here, I'm using Glide library as an example.
            // Make sure to add the necessary dependencies for Glide in your app build.gradle.
            Glide.with(this)
                .load(uri)
                .into(imageView)
        }.addOnFailureListener {
            // Handle the case when image download fails.
            // You can show a placeholder image or handle the failure as needed.
            // For simplicity, I'm just displaying a toast here.
            Toast.makeText(this@MainActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

}