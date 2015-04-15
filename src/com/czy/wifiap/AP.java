package com.czy.wifiap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class AP {
	
	private static final String INT_PRIVATE_KEY = "private_key";
	 private static final String INT_PHASE2 = "phase2";
	 private static final String INT_PASSWORD = "password";
	 private static final String INT_IDENTITY = "identity";
	 private static final String INT_EAP = "eap";
	 private static final String INT_CLIENT_CERT = "client_cert";
	 private static final String INT_CA_CERT = "ca_cert";
	 private static final String INT_ANONYMOUS_IDENTITY = "anonymous_identity";
	 final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
	void readEapConfig(BufferedWriter out , Context context) {
		/* Get the WifiService */
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		/* Get All WIfi configurations */
		List<WifiConfiguration> configList = wifi.getConfiguredNetworks();
		/*
		 * Now we need to search appropriate configuration i.e. with name
		 * SSID_Name
		 */
		for (int i = 0; i < configList.size(); i++) {
			{
				/* We found the appropriate config now read all config details */
				Iterator<WifiConfiguration> iter = configList.iterator();
				WifiConfiguration config = configList.get(i);

				/*
				 * I dont think these fields have anything to do with EAP config
				 * but still will print these to be on safe side
				 */
				try {
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[SSID]"
							+ config.SSID);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>" + "[SSID]"
							+ config.SSID);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[BSSID]"
							+ config.BSSID);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[BSSID]" + config.BSSID);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[HIDDEN SSID]" + config.hiddenSSID);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[HIDDEN SSID]" + config.hiddenSSID);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[PASSWORD]"
							+ config.preSharedKey);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[PASSWORD]" + config.preSharedKey);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[ALLOWED ALGORITHMS]");
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[ALLOWED ALGORITHMS]");
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[LEAP]"
									+ config.allowedAuthAlgorithms
											.get(AuthAlgorithm.LEAP));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[LEAP]"
							+ config.allowedAuthAlgorithms
									.get(AuthAlgorithm.LEAP));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[OPEN]"
									+ config.allowedAuthAlgorithms
											.get(AuthAlgorithm.OPEN));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[OPEN]"
							+ config.allowedAuthAlgorithms
									.get(AuthAlgorithm.OPEN));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[SHARED]"
									+ config.allowedAuthAlgorithms
											.get(AuthAlgorithm.SHARED));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[SHARED]"
							+ config.allowedAuthAlgorithms
									.get(AuthAlgorithm.SHARED));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[GROUP CIPHERS]");
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[GROUP CIPHERS]");
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[CCMP]"
							+ config.allowedGroupCiphers.get(GroupCipher.CCMP));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>" + "[CCMP]"
							+ config.allowedGroupCiphers.get(GroupCipher.CCMP));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[TKIP]"
							+ config.allowedGroupCiphers.get(GroupCipher.TKIP));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>" + "[TKIP]"
							+ config.allowedGroupCiphers.get(GroupCipher.TKIP));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[WEP104]"
									+ config.allowedGroupCiphers
											.get(GroupCipher.WEP104));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP104]"
							+ config.allowedGroupCiphers
									.get(GroupCipher.WEP104));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WEP40]"
							+ config.allowedGroupCiphers.get(GroupCipher.WEP40));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP40]"
							+ config.allowedGroupCiphers.get(GroupCipher.WEP40));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[KEYMGMT]");
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[KEYMGMT]");
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[IEEE8021X]"
									+ config.allowedKeyManagement
											.get(KeyMgmt.IEEE8021X));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[IEEE8021X]"
							+ config.allowedKeyManagement
									.get(KeyMgmt.IEEE8021X));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[NONE]"
							+ config.allowedKeyManagement.get(KeyMgmt.NONE));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>" + "[NONE]"
							+ config.allowedKeyManagement.get(KeyMgmt.NONE));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WPA_EAP]"
							+ config.allowedKeyManagement.get(KeyMgmt.WPA_EAP));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WPA_EAP]"
							+ config.allowedKeyManagement.get(KeyMgmt.WPA_EAP));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WPA_PSK]"
							+ config.allowedKeyManagement.get(KeyMgmt.WPA_PSK));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WPA_PSK]"
							+ config.allowedKeyManagement.get(KeyMgmt.WPA_PSK));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[PairWiseCipher]");
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[PairWiseCipher]");
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[CCMP]"
									+ config.allowedPairwiseCiphers
											.get(PairwiseCipher.CCMP));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[CCMP]"
							+ config.allowedPairwiseCiphers
									.get(PairwiseCipher.CCMP));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[NONE]"
									+ config.allowedPairwiseCiphers
											.get(PairwiseCipher.NONE));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[NONE]"
							+ config.allowedPairwiseCiphers
									.get(PairwiseCipher.NONE));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[TKIP]"
									+ config.allowedPairwiseCiphers
											.get(PairwiseCipher.TKIP));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[TKIP]"
							+ config.allowedPairwiseCiphers
									.get(PairwiseCipher.TKIP));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[Protocols]");
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[Protocols]");
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[RSN]"
							+ config.allowedProtocols.get(Protocol.RSN));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>" + "[RSN]"
							+ config.allowedProtocols.get(Protocol.RSN));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WPA]"
							+ config.allowedProtocols.get(Protocol.WPA));
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>" + "[WPA]"
							+ config.allowedProtocols.get(Protocol.WPA));
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[PRE_SHARED_KEY]" + config.preSharedKey);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[PRE_SHARED_KEY]" + config.preSharedKey);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"[WEP Key Strings]");
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP Key Strings]");
					String[] wepKeys = config.wepKeys;
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WEP KEY 0]"
							+ wepKeys[0]);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP KEY 0]" + wepKeys[0]);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WEP KEY 1]"
							+ wepKeys[1]);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP KEY 1]" + wepKeys[1]);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WEP KEY 2]"
							+ wepKeys[2]);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP KEY 2]" + wepKeys[2]);
					Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[WEP KEY 3]"
							+ wepKeys[3]);
					out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
							+ "[WEP KEY 3]" + wepKeys[3]);

				} catch (IOException e) {
					Toast toast1 = Toast.makeText(context,
							"Failed to write Logs to ReadConfigLog.txt", 3000);
					Toast toast2 = Toast.makeText(context,
							"Please take logs using Logcat", 5000);
					Log.e("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"Could not write to ReadConfigLog.txt"
									+ e.getMessage());
				}
				/* reflection magic */
				/* These are the fields we are really interested in */
				try {
					// Let the magic start
					Class[] wcClasses = WifiConfiguration.class.getClasses();
					// null for overzealous java compiler
					Class wcEnterpriseField = null;

					for (Class wcClass : wcClasses)
						if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME)) {
							wcEnterpriseField = wcClass;
							break;
						}
					boolean noEnterpriseFieldType = false;
					if (wcEnterpriseField == null)
						noEnterpriseFieldType = true; // Cupcake/Donut access
														// enterprise settings
														// directly

					Field wcefAnonymousId = null, wcefCaCert = null, wcefClientCert = null, wcefEap = null, wcefIdentity = null, wcefPassword = null, wcefPhase2 = null, wcefPrivateKey = null;
					Field[] wcefFields = WifiConfiguration.class.getFields();
					// Dispatching Field vars
					for (Field wcefField : wcefFields) {
						if (wcefField.getName().trim()
								.equals(INT_ANONYMOUS_IDENTITY))
							wcefAnonymousId = wcefField;
						else if (wcefField.getName().trim().equals(INT_CA_CERT))
							wcefCaCert = wcefField;
						else if (wcefField.getName().trim()
								.equals(INT_CLIENT_CERT))
							wcefClientCert = wcefField;
						else if (wcefField.getName().trim().equals(INT_EAP))
							wcefEap = wcefField;
						else if (wcefField.getName().trim()
								.equals(INT_IDENTITY))
							wcefIdentity = wcefField;
						else if (wcefField.getName().trim()
								.equals(INT_PASSWORD))
							wcefPassword = wcefField;
						else if (wcefField.getName().trim().equals(INT_PHASE2))
							wcefPhase2 = wcefField;
						else if (wcefField.getName().trim()
								.equals(INT_PRIVATE_KEY))
							wcefPrivateKey = wcefField;
					}
					Method wcefValue = null;
					if (!noEnterpriseFieldType) {
						for (Method m : wcEnterpriseField.getMethods())
							// System.out.println(m.getName());
							if (m.getName().trim().equals("value")) {
								wcefValue = m;
								break;
							}
					}

					/* EAP Method */
					String result = null;
					Object obj = null;
					if (!noEnterpriseFieldType) {
						obj = wcefValue.invoke(wcefEap.get(config), null);
						String retval = (String) obj;
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP METHOD]" + retval);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP METHOD]" + retval);
					} else {
						obj = wcefEap.get(config);
						String retval = (String) obj;
					}

					/* phase 2 */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefPhase2.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP PHASE 2 AUTHENTICATION]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP PHASE 2 AUTHENTICATION]" + result);
					} else {
						result = (String) wcefPhase2.get(config);
					}

					/* Anonymous Identity */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefAnonymousId.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP ANONYMOUS IDENTITY]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP ANONYMOUS IDENTITY]" + result);
					} else {
						result = (String) wcefAnonymousId.get(config);
					}

					/* CA certificate */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefCaCert.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP CA CERTIFICATE]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP CA CERTIFICATE]" + result);
					} else {
						result = (String) wcefCaCert.get(config);

					}

					/* private key */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefPrivateKey.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP PRIVATE KEY]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP PRIVATE KEY]" + result);
					} else {
						result = (String) wcefPrivateKey.get(config);
					}

					/* Identity */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefIdentity.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP IDENTITY]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP IDENTITY]" + result);
					} else {
						result = (String) wcefIdentity.get(config);
					}

					/* Password */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefPassword.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP PASSWORD]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP PASSWORD]" + result);
					} else {
						result = (String) wcefPassword.get(config);
					}

					/* client certificate */
					if (!noEnterpriseFieldType) {
						result = (String) wcefValue.invoke(
								wcefClientCert.get(config), null);
						Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
								"[EAP CLIENT CERT]" + result);
						out.write("<<<<<<<<<<WifiPreference>>>>>>>>>>>>"
								+ "[EAP CLIENT CERT]" + result);
					} else {
						result = (String) wcefClientCert.get(config);
					}

					out.close();

				} catch (IOException e) {
					Log.e("<<<<<<<<<<WifiPreference>>>>>>>>>>>>",
							"Could not write to ReadConfigLog.txt"
									+ e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}
