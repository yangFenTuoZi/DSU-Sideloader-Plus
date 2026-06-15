package yangfentuozi.dsusideloaderplus.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import yangfentuozi.dsusideloaderplus.ui.screen.about.AboutScreen
import yangfentuozi.dsusideloaderplus.ui.screen.adb.AdbScreen
import yangfentuozi.dsusideloaderplus.ui.screen.home.Home
import yangfentuozi.dsusideloaderplus.ui.screen.images.Images
import yangfentuozi.dsusideloaderplus.ui.screen.libraries.LibrariesScreen
import yangfentuozi.dsusideloaderplus.ui.screen.settings.Settings

object Destinations {
    const val Homepage = "home"
    const val Images = "images"
    const val Preferences = "preferences"
    const val ADBInstallation = "adb_installation"
    const val About = "about"
    const val Libraries = "libraries"
    const val Up = "up"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.Homepage) {
        fun navigate(destination: String) {
            if (destination == Destinations.Up) {
                navController.navigateUp()
            } else {
                navController.navigate(destination)
            }
        }

        composable(Destinations.Homepage) { Home(navigate = { navigate(it) }) }
        composable(Destinations.Images) { Images(navigate = { navigate(it) }) }
        composable(Destinations.Preferences) { Settings(navigate = { navigate(it) }) }
        composable(Destinations.ADBInstallation) { AdbScreen(navigate = { navigate(it) }) }
        composable(Destinations.About) { AboutScreen(navigate = { navigate(it) }) }
        composable(Destinations.Libraries) { LibrariesScreen(navigate = { navigate(it) }) }
    }
}
