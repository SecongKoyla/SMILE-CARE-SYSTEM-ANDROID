package com.android.smilecare.screens.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.utils.getEditTextValue
import com.android.smilecare.utils.toast

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var presenter: RegisterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        presenter = RegisterPresenter(this, RegisterModel(application as CustomApp))

        findViewById<Button>(R.id.buttonRegister).setOnClickListener {
            presenter.register(
                getEditTextValue(R.id.edittextFirstName),
                getEditTextValue(R.id.edittextLastName),
                getEditTextValue(R.id.edittextEmail),
                getEditTextValue(R.id.edittextPassword),
                getEditTextValue(R.id.edittextConfirmPassword)
            )
        }

        findViewById<TextView>(R.id.textLogin).setOnClickListener { finish() }
    }

    override fun showEmptyFieldsMessage() = toast("Please fill in all fields.")
    override fun showEmailAlreadyExistsMessage() = toast("Email is already registered.")
    override fun showPasswordMismatchMessage() = toast("Passwords do not match.")
    override fun showSuccessMessage() = toast("Account created successfully!")
    override fun showLoginScreen() = finish()
}
