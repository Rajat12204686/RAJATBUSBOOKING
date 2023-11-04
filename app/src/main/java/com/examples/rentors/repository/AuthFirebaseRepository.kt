package com.examples.rentors.repository

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.examples.rentors.domain.User
import java.util.concurrent.TimeUnit


class AuthFirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = Firebase.database.reference
    var user: MutableLiveData<FirebaseUser> = MutableLiveData()
    var wrongCode: MutableLiveData<Boolean> = MutableLiveData()
    var codeSent = ObservableBoolean()
    var newUser = ObservableBoolean()
    var loggedIn = ObservableBoolean()
    private var storedVerificationId: MutableLiveData<String> = MutableLiveData()
    private var resendToken: MutableLiveData<PhoneAuthProvider.ForceResendingToken> =
        MutableLiveData()

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {}
                is FirebaseTooManyRequestsException -> {}
                is FirebaseAuthMissingActivityForRecaptchaException -> {}
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            storedVerificationId.postValue(verificationId)
            resendToken.postValue(token)
            codeSent.set(true)
        }
    }

    init {
        loggedIn.set(auth.currentUser != null)
        if (auth.currentUser != null) {
            try {
                auth.currentUser?.getIdToken(true)
                user.postValue(auth.currentUser)
            } catch (e: FirebaseAuthInvalidUserException) {
                logout()
            }
        }
    }

    private fun logout() = auth.signOut()

    fun sendCodeToPhone(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setCallbacks(callbacks).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun sendCodeToVerify(code: String) {
        signInWithPhoneAuthCredential(
            PhoneAuthProvider.getCredential(
                storedVerificationId.value.toString(), code
            )
        )
    }

    fun writeUserToDatabase(name: String, lastname: String) {
        val user =
            User(auth.uid.toString(), name, lastname, auth.currentUser?.phoneNumber.toString())
        databaseReference.child("users").child(user.uid).setValue(user)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                databaseReference.child("users").get().addOnSuccessListener { snapshot ->
                    val isNewUser =
                        snapshot.children.map { it.getValue<User>()!! }.none { it.uid == auth.uid }
                    newUser.set(isNewUser)
                    loggedIn.set(true)
                    Log.d("Auth", isNewUser.toString())
                }
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    wrongCode.postValue(true)
                }
            }
        }
    }

}