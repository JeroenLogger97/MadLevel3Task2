package com.example.madlevel3task3

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_portals.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PortalsFragment : Fragment() {

    private var customTabHelper: CustomTabHelper = CustomTabHelper()
    private val portals = arrayListOf<Portal>()

    // set onClickListener to itemClicked()
    private val portalAdapter = PortalAdapter(portals) { portal -> itemClicked(portal) }

    private fun itemClicked(portal: Portal) {
        openBrowser(portal.url)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_portals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        observeAddPortalResult()
    }

    private fun initViews() {
        // Initialize the recycler view with a linear layout manager, adapter
        rvPortals.layoutManager = GridLayoutManager(context, 2)
        rvPortals.adapter = portalAdapter
    }

    // retrieve bundle data from add_portal view
    private fun observeAddPortalResult() {
        setFragmentResultListener(REQ_REMINDER_KEY) { _, bundle ->
            val title = bundle.getString(BUNDLE_TITLE_KEY)
            val url = bundle.getString(BUNDLE_URL_KEY)

            title?.let {it ->
                val portal = url?.let { url -> Portal(it, url) } ?: onEmptyRequest()

                portals.add(portal as Portal)
                portalAdapter.notifyDataSetChanged()
            } ?: onEmptyRequest()

        }
    }

    private fun onEmptyRequest() {
        Log.e("PortalFragment", "Request triggered, but empty portal field!")
    }

    private fun openBrowser(uri: String) {
        val builder = CustomTabsIntent.Builder()

        // modify toolbar color
        context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }?.let {
            builder.setToolbarColor(
                it
            )
        }

        // add share button to overflow menu
        builder.addDefaultShareMenuItem()

        val anotherCustomTab = CustomTabsIntent.Builder().build()

        val requestCode = 100
        val intent = anotherCustomTab.intent
        intent.data = Uri.parse(uri)

        val pendingIntent = PendingIntent.getActivity(context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        // add menu item to oveflow
        builder.addMenuItem("Sample item", pendingIntent)

        // menu item icon
        // val bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)
        // builder.setActionButton(bitmap, "Android", pendingIntent, true)

        // modify back button icon
        // builder.setCloseButtonIcon(bitmap)

        // show website title
        builder.setShowTitle(true)

        // animation for enter and exit of tab
        context?.let { builder.setStartAnimations(it, android.R.anim.fade_in, android.R.anim.fade_out) }
        context?.let { builder.setExitAnimations(it, android.R.anim.fade_in, android.R.anim.fade_out) }

        val customTabsIntent = builder.build()

        // check is chrom available
        val packageName =
            context?.let { customTabHelper.getPackageNameToUse(it, uri) }

        if (packageName == null) {
            // if chrome not available open in web view
            val intentOpenUri = Intent(context, PortalsFragment::class.java)
            intentOpenUri.putExtra(EXTRA_URL, Uri.parse(uri).toString())
            startActivity(intentOpenUri)
        } else {
            customTabsIntent.intent.setPackage(packageName)
            context?.let { customTabsIntent.launchUrl(it, Uri.parse(uri)) }
        }
    }

}