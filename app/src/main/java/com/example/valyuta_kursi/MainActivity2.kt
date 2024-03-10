package com.example.valyuta_kursi

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.valyuta_kursi.databinding.ActivityMain2Binding
import org.json.JSONArray
import org.json.JSONException

class MainActivity2 : AppCompatActivity() {
    private val binding by lazy { ActivityMain2Binding.inflate(layoutInflater) }
    private lateinit var listName: Array<String>
    private lateinit var requestQueue: RequestQueue
    private var dot = true
    private val url = "http://cbu.uz/uzc/arkhiv-kursov-valyut/json/"
    private var fromCurrencyRate: Double = 0.0
    private var toCurrencyRate: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        requestQueue = Volley.newRequestQueue(binding.root.context)

        fetchObjectLoad()

        setupButtons()
    }

    private fun setupButtons() {
        val numberButtons = arrayOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                appendNumber(index.toString())
            }
        }

        binding.btnDot.setOnClickListener {
            if (binding.n0.text.first() == '0' && !dot) {
                binding.n0.append(".")
                dot = false
            }
        }

        binding.clear.setOnClickListener {
            binding.n0.text = "0"
            binding.n1.text = "0"
            dot = true
        }

        binding.btnConvert.setOnClickListener {
            val amount = binding.n0.text.toString().toDoubleOrNull()
            val fromCurrency = listName[binding.fromSpinner.selectedItemPosition]
            val toCurrency = listName[binding.toSpinner.selectedItemPosition]

            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        for (i in 0 until response.length()) {
                            val currency = response.getJSONObject(i)
                            val currencyCode = currency.getString("Code")
                            val currencyRate = currency.getDouble("Rate")

                            if (currencyCode == fromCurrency) {
                                fromCurrencyRate = currencyRate
                            }

                            if (currencyCode == toCurrency) {
                                toCurrencyRate = currencyRate
                            }
                        }

                        val convertedAmount = convertCurrency(amount, fromCurrencyRate, toCurrencyRate)
                        binding.n1.text = " $convertedAmount $toCurrency"

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Conversion", "Error: ${error.message}")
                }
            )

            requestQueue.add(jsonArrayRequest)
        }
    }

    private fun appendNumber(number: String) {
        if (binding.n0.text.first() == '0' && binding.n0.text.length == 1) {
            binding.n0.text = number
        } else {
            binding.n0.append(number)
        }
    }

    private fun convertCurrency(amount: Double, fromRate: Double, toRate: Double): Double? {
        return if (fromRate  != 1.0 && toRate != 1.0) {
        (amount * toRate) / fromRate
    } else {
        0.0
    }
    }

    private fun fetchObjectLoad() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val currencies: MutableSet<String> = HashSet()
                for (i in 0 until response.length()) {
                    try {
                        val jsonObject = response.getJSONObject(i)
                        val currency = jsonObject.getString("Ccy")
                        currencies.add(currency)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                val currencyArray = currencies.toTypedArray()
                listName = currencyArray
                val adapter = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    currencyArray
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.toSpinner.adapter = adapter
                binding.fromSpinner.adapter = adapter
            },
            { error ->
                Log.d("ASD", error.message.toString())
                Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }
    fun convert(valyuta1: Api, valyuta2: Api):Float{
        return valyuta2.Rate.toFloat()/valyuta1.Rate.toFloat()
    }
}
