package yangfentuozi.dsusideloaderplus.service

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.content.Intent
import android.content.pm.IPackageManager
import android.gsi.GsiProgress
import android.gsi.IGsiService
import android.gsi.IImageService
import android.gsi.MappedImage
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.SystemProperties
import android.os.image.IDynamicSystemService
import android.os.storage.IStorageManager
import android.os.storage.VolumeInfo
import android.system.Os
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.exitProcess
import org.lsposed.hiddenapibypass.HiddenApiBypass
import yangfentuozi.dsusideloaderplus.BuildConfig
import yangfentuozi.dsusideloaderplus.IPrivilegedService

class PrivilegedService : IPrivilegedService.Stub() {

    override fun exit() {
        destroy()
    }

    override fun destroy() {
        exitProcess(0)
    }

    @SuppressLint("PrivateApi")
    private fun getBinderOrNull(service: String): IBinder? {
        val serviceManager = Class.forName("android.os.ServiceManager")
        val binder = HiddenApiBypass.invoke(serviceManager, null, "getService", service)
        return binder as? IBinder
    }

    private fun getBinder(service: String): IBinder {
        return getBinderOrNull(service)
            ?: throw IllegalStateException("$service service returned null")
    }

    fun setProp(key: String, value: String) {
        try {
            SystemProperties.set(key, value)
        } catch (e: Exception) {
            Log.w(BuildConfig.APPLICATION_ID, e.stackTraceToString())
        }
    }

    override fun setDynProp() {
        setProp("persist.sys.fflag.override.settings_dynamic_system", "true")
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    //
    // Activity Manager
    //

    private var activityManager: IActivityManager? = null

    private fun requiresActivityManager() {
        if (activityManager == null) {
            activityManager = IActivityManager.Stub.asInterface(getBinder("activity"))
        }
    }

    override fun startActivity(intent: Intent?) {
        requiresActivityManager()
        val callerPackage =
            if (uid == 2000 || uid == 0) "com.android.shell" else BuildConfig.APPLICATION_ID

        if (Build.VERSION.SDK_INT > 29) {
            activityManager!!.startActivityAsUserWithFeature(
                null,
                callerPackage,
                null,
                intent,
                null,
                null,
                null,
                0,
                0,
                null,
                null,
                0,
            )
        } else {
            activityManager!!.startActivityAsUser(
                null,
                callerPackage,
                intent,
                null,
                null,
                null,
                0,
                0,
                null,
                null,
                0,
            )
        }
    }

    override fun forceStopPackage(packageName: String?) {
        requiresActivityManager()
        activityManager!!.forceStopPackage(packageName, 0)
    }

    //
    // Package Manager
    //

    private var packageManager: IPackageManager? = null

    private fun requiresPackageManager() {
        if (packageManager == null) {
            packageManager = IPackageManager.Stub.asInterface(getBinder("package"))
        }
    }

    override fun grantPermission(permissionName: String?) {
        requiresPackageManager()
        packageManager!!.grantRuntimePermission(BuildConfig.APPLICATION_ID, permissionName, 0)
    }

    //
    // Storage Manager
    //

    private var storageManager: IStorageManager? = null

    private fun requiresStorageManager() {
        if (storageManager == null) {
            storageManager = IStorageManager.Stub.asInterface(getBinder("mount"))
        }
    }

    override fun getVolumes(): List<VolumeInfo> {
        requiresStorageManager()
        val vols = ArrayList<VolumeInfo>()
        vols.addAll(storageManager!!.getVolumes(0))
        return vols
    }

    override fun unmount(volId: String?) {
        requiresStorageManager()
        storageManager!!.unmount(volId)
    }

    override fun mount(volId: String?) {
        requiresStorageManager()
        storageManager!!.mount(volId)
    }

    /**
     * Dynamic System Service
     *
     * Most methods are using @EnforcePermission("MANAGE_DYNAMIC_SYSTEM")
     * they are only accessible via root or as system app (proper installed)
     * Shizuku is able to call those methods, but they won't work as shell (2000)
     * since MANAGE_DYNAMIC_SYSTEM is required, and shell does not have it
     *
     * On stock Android, shell is able to install GSIs via DSU over Dynamic System Updates app
     * that has MANAGE_DYNAMIC_SYSTEM permission, shell has only INSTALL_DYNAMIC_SYSTEM
     */

    private var dynamicSystemService: IDynamicSystemService? = null

    private fun requiresDynamicSystem() {
        if (dynamicSystemService == null) {
            dynamicSystemService = IDynamicSystemService.Stub.asInterface(getBinder("dynamic_system"))
        }
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun closePartition(): Boolean {
        if (Build.VERSION.SDK_INT <= 30) {
            // Android R does not seem to close partition?
            // closePartition() was implemented on S
            return true
        }
        requiresDynamicSystem()
        return dynamicSystemService!!.closePartition()
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun finishInstallation(): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.finishInstallation()
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun getInstallationProgress(): GsiProgress? {
        requiresDynamicSystem()
        return dynamicSystemService!!.installationProgress
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun abort(): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.abort()
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun isEnabled(): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.isEnabled
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun remove(): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.remove()
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun setEnable(enable: Boolean, oneShot: Boolean): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.setEnable(enable, oneShot)
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun startInstallation(dsuSlot: String?): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.startInstallation(dsuSlot)
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun createPartition(name: String?, size: Long, readOnly: Boolean): Int {
        requiresDynamicSystem()
        // Below T, createPartition returns boolean
        if (Build.VERSION.SDK_INT < 33) {
            val result = HiddenApiBypass.invoke(
                dynamicSystemService!!.javaClass,
                dynamicSystemService!!,
                "createPartition",
                name,
                size,
                readOnly,
            )
            return if (result as Boolean) IGsiService.INSTALL_OK else IGsiService.INSTALL_ERROR_GENERIC
        }
        return dynamicSystemService!!.createPartition(name, size, readOnly)
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun setAshmem(fd: ParcelFileDescriptor?, size: Long): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.setAshmem(fd, size)
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun submitFromAshmem(bytes: Long): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.submitFromAshmem(bytes)
    }

    // REQUIRES MANAGE_DYNAMIC_SYSTEM
    override fun suggestScratchSize(): Long {
        requiresDynamicSystem()
        return dynamicSystemService!!.suggestScratchSize()
    }

    override fun isInUse(): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.isInUse
    }

    override fun isInstalled(): Boolean {
        requiresDynamicSystem()
        return dynamicSystemService!!.isInstalled
    }

    //
    // GSI backing image service
    //

    private var gsiService: IGsiService? = null

    private fun requiresGsiService(): IGsiService {
        if (gsiService == null) {
            gsiService = IGsiService.Stub.asInterface(getGsiServiceBinder())
        }
        return gsiService!!
    }

    private fun getGsiServiceBinder(): IBinder {
        getBinderOrNull("gsiservice")?.let { return it }
        setProp("ctl.start", "gsid")

        val deadline = System.currentTimeMillis() + 10_000L
        while (System.currentTimeMillis() < deadline) {
            getBinderOrNull("gsiservice")?.let { return it }
            Thread.sleep(100L)
        }
        throw IllegalStateException("gsiservice is not available after starting gsid")
    }

    override fun getInstalledDsuSlots(): List<String> {
        return requiresGsiService().getInstalledDsuSlots()
    }

    override fun getActiveDsuSlot(): String {
        return requiresGsiService().getActiveDsuSlot()
    }

    override fun getInstalledGsiImageDir(): String {
        return requiresGsiService().getInstalledGsiImageDir()
    }

    override fun getDsuBackingImages(prefix: String?): List<String> {
        validateGsiPrefix(prefix)
        return requiresGsiService()
            .openImageService(prefix!!)
            .getAllBackingImages()
    }

    override fun deleteDsuBackingImage(prefix: String?, imageName: String?): String {
        return runCatching {
            validateGsiPrefix(prefix)
            validateGsiImageName(imageName)
            val imageService = requiresGsiService().openImageService(prefix!!)
            val deleted = deleteDsuBackingImage(imageService, imageName!!)
            if (deleted) "" else "Image not found: $imageName"
        }.getOrElse {
            Log.e(BuildConfig.APPLICATION_ID, it.stackTraceToString())
            it.message ?: it.toString()
        }
    }

    override fun replaceDsuBackingImage(
        prefix: String?,
        imageName: String?,
        imageFd: ParcelFileDescriptor?,
        imageSize: Long,
        readOnly: Boolean,
    ): String {
        return runCatching {
            validateGsiPrefix(prefix)
            validateGsiImageName(imageName)
            requireNotNull(imageFd) { "input image fd is null" }

            ensureGsiServiceDirectory("/data/gsi/$prefix")
            ensureGsiServiceDirectory("/metadata/gsi/$prefix")

            val imageService = requiresGsiService().openImageService(prefix!!)
            replaceDsuBackingImage(imageService, imageName!!, imageFd, imageSize, readOnly)
            ""
        }.getOrElse {
            Log.e(BuildConfig.APPLICATION_ID, it.stackTraceToString())
            it.message ?: it.toString()
        }.also {
            runCatching { imageFd?.close() }
        }
    }

    override fun addDsuBackingImage(
        prefix: String?,
        imageName: String?,
        imageFd: ParcelFileDescriptor?,
        imageSize: Long,
        readOnly: Boolean,
    ): String {
        return runCatching {
            validateGsiPrefix(prefix)
            validateGsiImageName(imageName)
            requireNotNull(imageFd) { "input image fd is null" }

            ensureGsiServiceDirectory("/data/gsi/$prefix")
            ensureGsiServiceDirectory("/metadata/gsi/$prefix")

            val imageService = requiresGsiService().openImageService(prefix!!)
            addDsuBackingImage(imageService, imageName!!, imageFd, imageSize, readOnly)
            ""
        }.getOrElse {
            Log.e(BuildConfig.APPLICATION_ID, it.stackTraceToString())
            it.message ?: it.toString()
        }.also {
            runCatching { imageFd?.close() }
        }
    }

    override fun exportDsuBackingImage(
        prefix: String?,
        imageName: String?,
        imageFd: ParcelFileDescriptor?,
    ): String {
        return runCatching {
            validateGsiPrefix(prefix)
            validateGsiImageName(imageName)
            requireNotNull(imageFd) { "output image fd is null" }

            val imageService = requiresGsiService().openImageService(prefix!!)
            exportDsuBackingImage(imageService, imageName!!, imageFd)
            ""
        }.getOrElse {
            Log.e(BuildConfig.APPLICATION_ID, it.stackTraceToString())
            it.message ?: it.toString()
        }.also {
            runCatching { imageFd?.close() }
        }
    }

    private fun deleteDsuBackingImage(
        imageService: IImageService,
        imageName: String,
    ): Boolean {
        if (imageService.isImageMapped(imageName)) {
            imageService.unmapImageDevice(imageName)
        }
        if (!imageService.backingImageExists(imageName)) {
            return false
        }
        imageService.deleteBackingImage(imageName)
        return true
    }

    private fun replaceDsuBackingImage(
        imageService: IImageService,
        imageName: String,
        imageFd: ParcelFileDescriptor,
        imageSize: Long,
        readOnly: Boolean,
    ) {
        writeDsuBackingImage(
            imageService = imageService,
            imageName = imageName,
            imageFd = imageFd,
            imageSize = imageSize,
            readOnly = readOnly,
            replaceExisting = true,
        )
    }

    private fun addDsuBackingImage(
        imageService: IImageService,
        imageName: String,
        imageFd: ParcelFileDescriptor,
        imageSize: Long,
        readOnly: Boolean,
    ) {
        writeDsuBackingImage(
            imageService = imageService,
            imageName = imageName,
            imageFd = imageFd,
            imageSize = imageSize,
            readOnly = readOnly,
            replaceExisting = false,
        )
    }

    private fun exportDsuBackingImage(
        imageService: IImageService,
        imageName: String,
        imageFd: ParcelFileDescriptor,
    ) {
        if (!imageService.backingImageExists(imageName)) {
            throw IllegalStateException("Image not found: $imageName")
        }
        if (imageService.isImageMapped(imageName)) {
            imageService.unmapImageDevice(imageName)
        }

        var mapped = false
        try {
            val mappedImage = MappedImage()
            imageService.mapImageDevice(imageName, IMAGE_SERVICE_WAIT_MS, mappedImage)
            mapped = true

            val mappedPath = mappedImage.path
                ?: throw IllegalStateException("mapImageDevice($imageName) returned empty path")
            copyBlockDeviceToFile(mappedPath, imageFd)
        } finally {
            if (mapped) {
                runCatching { imageService.unmapImageDevice(imageName) }
            }
        }
    }

    private fun writeDsuBackingImage(
        imageService: IImageService,
        imageName: String,
        imageFd: ParcelFileDescriptor,
        imageSize: Long,
        readOnly: Boolean,
        replaceExisting: Boolean,
    ) {
        require(imageSize > 0) { "input image is empty" }
        require(imageSize % 512L == 0L) { "input image size must be 512-byte aligned: $imageSize" }

        val exists = imageService.backingImageExists(imageName)
        if (exists && !replaceExisting) {
            throw IllegalStateException("Image already exists: $imageName")
        }
        val isMapped = imageService.isImageMapped(imageName)
        if (isMapped && !replaceExisting) {
            throw IllegalStateException("Image already exists: $imageName")
        }
        if (isMapped) {
            imageService.unmapImageDevice(imageName)
        }
        if (exists) {
            imageService.deleteBackingImage(imageName)
        }

        var created = false
        var mapped = false
        try {
            val flags =
                if (readOnly) IImageService.CREATE_IMAGE_READONLY else IImageService.CREATE_IMAGE_DEFAULT
            imageService.createBackingImage(imageName, imageSize, flags, null)
            created = true

            val mappedImage = MappedImage()
            imageService.mapImageDevice(imageName, IMAGE_SERVICE_WAIT_MS, mappedImage)
            mapped = true

            val mappedPath = mappedImage.path
                ?: throw IllegalStateException("mapImageDevice($imageName) returned empty path")
            copyFileToBlockDevice(imageFd, mappedPath, imageSize)
        } catch (e: Exception) {
            if (mapped) {
                runCatching { imageService.unmapImageDevice(imageName) }
            }
            if (created) {
                runCatching { imageService.deleteBackingImage(imageName) }
            }
            throw e
        }

        imageService.unmapImageDevice(imageName)
    }

    private fun copyBlockDeviceToFile(
        mappedPath: String,
        imageFd: ParcelFileDescriptor,
    ) {
        val buffer = ByteArray(4 * 1024 * 1024)
        FileInputStream(mappedPath).use { input ->
            ParcelFileDescriptor.AutoCloseOutputStream(imageFd).use { output ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) {
                        break
                    }
                    output.write(buffer, 0, read)
                }
                output.fd.sync()
            }
        }
    }

    private fun copyFileToBlockDevice(
        imageFd: ParcelFileDescriptor,
        mappedPath: String,
        imageSize: Long,
    ) {
        val buffer = ByteArray(4 * 1024 * 1024)
        var copied = 0L
        ParcelFileDescriptor.AutoCloseInputStream(imageFd).use { input ->
            FileOutputStream(mappedPath).use { output ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) {
                        break
                    }
                    output.write(buffer, 0, read)
                    copied += read.toLong()
                }
                output.fd.sync()
            }
        }
        if (copied != imageSize) {
            throw IllegalStateException("short copy: copied $copied of $imageSize")
        }
    }

    private fun validateGsiPrefix(prefix: String?) {
        require(!prefix.isNullOrBlank()) { "prefix must not be empty" }
        require(!prefix.startsWith("/") && !prefix.contains("..")) {
            "prefix must be relative and must not contain '..': $prefix"
        }
    }

    private fun validateGsiImageName(imageName: String?) {
        require(!imageName.isNullOrBlank()) { "image name must not be empty" }
        require(!imageName.contains("/") && !imageName.contains("..")) {
            "image name must not contain '/' or '..': $imageName"
        }
    }

    private fun ensureGsiServiceDirectory(path: String) {
        val directory = File(path)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("failed to create $path")
        }
        Os.chmod(path, "0755".toInt(8))
        runCommand("/system/bin/restorecon", "-R", path)
    }

    private fun runCommand(vararg command: String) {
        val result = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()
            .waitFor()
        if (result != 0) {
            throw IllegalStateException("command failed ($result): ${command.joinToString(" ")}")
        }
    }

    private companion object {
        const val IMAGE_SERVICE_WAIT_MS = 10_000
    }
}
