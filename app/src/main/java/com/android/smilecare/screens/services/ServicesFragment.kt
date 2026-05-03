package com.android.smilecare.screens.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp

class ServicesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as CustomApp
        val list = view.findViewById<LinearLayout>(R.id.servicesList)

        app.services.forEach { service ->
            val item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_service, list, false)

            item.findViewById<TextView>(R.id.textServiceEmoji).text = service.emoji
            item.findViewById<TextView>(R.id.textServiceName).text = service.name
            item.findViewById<TextView>(R.id.textServiceDescription).text = service.description
            item.findViewById<TextView>(R.id.textServicePrice).text = "₱${service.price}"
            item.findViewById<TextView>(R.id.textServiceDuration).text =
                if (service.durationMinutes >= 60) "${service.durationMinutes / 60} hr" else "${service.durationMinutes} min"

            list.addView(item)
        }
    }
}
