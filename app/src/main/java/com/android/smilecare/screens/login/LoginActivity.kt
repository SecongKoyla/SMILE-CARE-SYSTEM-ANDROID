package com.android.smilecare.screens.login

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.screens.home.HomeActivity
import com.android.smilecare.screens.register.RegisterActivity
import com.android.smilecare.utils.getEditTextValue
import com.android.smilecare.utils.toast

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this, LoginModel(application as CustomApp))

        findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            val email = getEditTextValue(R.id.edittextEmail)
            val password = getEditTextValue(R.id.edittextPassword)
            presenter.login(email, password)
        }

        findViewById<TextView>(R.id.textRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun showSuccessMessage() = toast("Welcome back!")
    override fun showInvalidCredentialsMessage() = toast("Invalid email or password.")
    override fun showEmptyFieldsMessage() = toast("Please fill in all fields.")
    override fun showHomeScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
