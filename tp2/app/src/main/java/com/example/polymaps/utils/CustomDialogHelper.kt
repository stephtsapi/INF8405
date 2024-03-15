import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.polymaps.DetectedDevice
import com.example.polymaps.R

object CustomDialogHelper {
    fun buildCustomDialog(
        context: Context,
        list: ArrayList<DetectedDevice>,
        position: Int,
        onAddOrRemoveFavoriteClicked: (Int) -> Unit,
        onShareClicked:(Int) -> Unit,
        onHowtoGoCLicked:(Int) -> Unit,
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)
        val selectedItem = list[position]

        dialogView.findViewById<TextView>(R.id.dialogName).text = context.getString(R.string.device_name) + ": " + selectedItem.name
        dialogView.findViewById<TextView>(R.id.dialogMacAddress).text = context.getString(R.string.device_address) + ": " + selectedItem.macAddress
        dialogView.findViewById<TextView>(R.id.dialogPosition).text = context.getString(R.string.device_position) + " " + selectedItem.position.toString()
        dialogView.findViewById<TextView>(R.id.dialogType).text = context.getString(R.string.device_type) + ": " + selectedItem.type
        dialogView.findViewById<TextView>(R.id.dialogDistance).text = context.getString(R.string.device_distance) + ": " + selectedItem.distance

        val addToFavoritesButton = dialogView.findViewById<Button>(R.id.addToFavoritesButton)
        addToFavoritesButton.text = if (selectedItem.isFavorite) context.getString(R.string.remove_from_favorites) else context.getString(R.string.add_to_favorites)
        val shareButton = dialogView.findViewById<Button>(R.id.shareButton)
        val openGoogleMapsButton = dialogView.findViewById<Button>(R.id.howToGo)

        val dialog = dialogBuilder.create()
        dialog.show()
        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val params = window?.attributes
        params?.gravity = Gravity.CENTER

        addToFavoritesButton.setOnClickListener {
            onAddOrRemoveFavoriteClicked.invoke(position)
            dialog.dismiss()
        }

        shareButton.setOnClickListener {
            onShareClicked.invoke(position)
            dialog.dismiss()
        }

        openGoogleMapsButton.setOnClickListener {
            onHowtoGoCLicked.invoke(position)
            dialog.dismiss()
        }
    }
}