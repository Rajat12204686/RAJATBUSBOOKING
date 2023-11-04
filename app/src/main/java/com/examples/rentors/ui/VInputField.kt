package com.examples.rentors.ui

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.examples.rentors.R
import com.examples.rentors.domain.Stop


class VInputField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {


    private var icon: ImageView
    private var editText: EditText
    private var divider: View
    private var container: LinearLayout
    private var autoCompleteTextView: AutoCompleteTextView
    private var onClickListener: OnClickListener? = null
    private val codeInputs = mutableListOf<EditText>()

    init {
        val view = inflate(context, R.layout.v_input_field, this)
        icon = findViewById(R.id.v_input_icon)
        editText = findViewById(R.id.v_input_edit_text)
        divider = findViewById(R.id.v_input_divider)
        container = findViewById(R.id.v_input_container)
        autoCompleteTextView = findViewById(R.id.v_input_auto_complete)
        context.obtainStyledAttributes(attrs, R.styleable.VInputField, 0, 0).apply {
            icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    getResourceId(R.styleable.VInputField_v_icon, R.drawable.baseline_call_24),
                    null
                )
            )
            editText.hint = getString(R.styleable.VInputField_v_hint)
            autoCompleteTextView.hint = getString(R.styleable.VInputField_v_hint)
            when (getInt(R.styleable.VInputField_v_inputType, 0)) {
                1 -> editText.inputType = InputType.TYPE_CLASS_PHONE
                4 -> {
                    editText.inputType = InputType.TYPE_CLASS_DATETIME
                    isClickable = false
                    editText.isClickable = false
                    editText.isCursorVisible = false
                    isFocusable = false
                    editText.isFocusable = false
                }

                2 -> {
                    editText.visibility = View.GONE
                    autoCompleteTextView.visibility = View.VISIBLE
                }

                8 -> {
                    editText.visibility = View.GONE
                    autoCompleteTextView.visibility = View.GONE
                    icon.visibility = View.GONE
                    divider.visibility = View.GONE
                    container.setBackgroundResource(0)
                    val codeDigitsAmount = getInt(R.styleable.VInputField_v_code_length, 4)
                    for (i in 0 until codeDigitsAmount) {
                        val item = EditText(context, null)
                        item.setEms(1)
                        item.maxEms = 1
                        item.minEms = 1
                        val layoutParams = LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT
                        )
                        item.filters = arrayOf<InputFilter>(
                            LengthFilter(1)
                        )
                        item.addTextChangedListener(object : TextWatcher {
                            override fun onTextChanged(
                                s: CharSequence, start: Int, before: Int, count: Int
                            ) {
                                if (count >= 1) {
                                    val index = codeInputs.indexOf(item) + 1
                                    if (index < codeInputs.size) codeInputs[index].requestFocus()
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence, start: Int, count: Int, after: Int
                            ) {
                                // TODO Auto-generated method stub
                            }

                            override fun afterTextChanged(s: Editable) {
                                // TODO Auto-generated method stub
                            }
                        })
                        layoutParams.weight = 1f
                        item.layoutParams = layoutParams
                        container.setPadding(0)
                        item.setPadding(0)
                        item.gravity = Gravity.CENTER
                        item.setTextSize(COMPLEX_UNIT_SP, 24f)
                        codeInputs.add(item)
                        item.inputType = InputType.TYPE_CLASS_NUMBER
                        container.addView(codeInputs[i])
                    }
                }

                else -> editText.inputType = InputType.TYPE_CLASS_TEXT
            }

        }.also {
            it.recycle()
        }
    }

    fun getText(): String {
        return if (editText.visibility == View.VISIBLE) editText.text.toString()
        else {
            if (autoCompleteTextView.visibility == View.GONE) {
                val code = codeInputs.joinToString("") { item -> item.text.toString() }
                Log.d("Log", code)
                code
            } else {
                autoCompleteTextView.text.toString()
            }
        }
    }

    fun setText(text: String) = if (editText.visibility == View.VISIBLE) editText.setText(text)
    else autoCompleteTextView.setText(text)

    fun clearError() {
        editText.error = null
        autoCompleteTextView.error = null
    }

    fun setError(error: String) {
        if (editText.visibility == View.VISIBLE) editText.error = error
        else autoCompleteTextView.error = error
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            onClickListener?.onClick(this)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }

    fun setAdapter(adapter: ArrayAdapter<Stop>) = autoCompleteTextView.setAdapter(adapter)

}