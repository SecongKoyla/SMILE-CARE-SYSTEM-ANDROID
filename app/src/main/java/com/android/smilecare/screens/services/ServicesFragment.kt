package com.android.smilecare.screens.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.DentalService
import com.android.smilecare.screens.home.HomeActivity

class ServicesFragment : Fragment(), ServicesContract.View {

    private lateinit var presenter: ServicesContract.Presenter
    private lateinit var servicesList: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        servicesList = view.findViewById(R.id.servicesList)
        presenter = ServicesPresenter(this, ServicesModel(requireActivity().application as CustomApp))
        presenter.loadServices()
    }

    override fun showServices(services: List<DentalService>) {
        servicesList.removeAllViews()
        services.forEach { service ->
            val item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_service, servicesList, false)

            item.findViewById<TextView>(R.id.textServiceEmoji).text = service.emoji
            item.findViewById<TextView>(R.id.textServiceName).text = service.name
            item.findViewById<TextView>(R.id.textServiceDescription).text = service.description
            item.findViewById<TextView>(R.id.textServicePrice).text = "₱${service.price}"
            item.findViewById<TextView>(R.id.textServiceDuration).text =
                if (service.durationMinutes >= 60) "${service.durationMinutes / 60} hr" else "${service.durationMinutes} min"

            item.findViewById<Button>(R.id.buttonBookNow).setOnClickListener {
                (activity as? HomeActivity)?.navigateToBookAppointment(service.name)
            }

            servicesList.addView(item)
        }
    }
}
