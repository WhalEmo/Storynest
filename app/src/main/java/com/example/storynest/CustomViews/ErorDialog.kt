package com.example.storynest.CustomViews

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.DialogFragment
import com.example.storynest.databinding.DialogErrorBinding

class ErrorDialog : DialogFragment() {

    private var _binding: DialogErrorBinding? = null
    private val binding get() = _binding!!

    // Tıklama olayını tutacak değişken
    private var onOkClick: (() -> Unit)? = null

    // Fragment oluşturulurken tasarımımızı yüklüyoruz
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogErrorBinding.inflate(inflater, container, false)
        return binding.root
    }


    // Viewler oluştuktan sonra verileri dolduruyoruz
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argümanlardan mesajı al
        val message = arguments?.getString(ARG_MESSAGE) ?: "Bir hata oluştu."
        val title = arguments?.getString(ARG_TITLE) ?: "Hata!"

        // UI elemanlarını ayarla
        binding.errorMessage.text = message
        binding.errorTitle.text = title

        binding.errorOkButton.setOnClickListener {
            onOkClick?.invoke()
            dismiss() // Dialogu kapat
        }
    }

    // Pencere ayarlarını burada yapıyoruz (Genişlik, Arka plan şeffaflığı vb.)
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
           window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Genişlik ayarı: Ekranın %90'ını kaplasın veya wrap_content olsun
            // XML'de 320dp verdiysen burayı WRAP_CONTENT yapabilirsin
            window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnOkClickListener(listener: () -> Unit) {
        this.onOkClick = listener
    }

    // Static metod (Best Practice: Fragment'ları böyle çağırmak daha güvenlidir)
    companion object {
        private const val ARG_MESSAGE = "message"
        private const val ARG_TITLE = "title"

        fun newInstance(title: String, message: String): ErrorDialog{
            val fragment = ErrorDialog()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }
}