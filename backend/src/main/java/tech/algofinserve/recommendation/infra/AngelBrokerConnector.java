package tech.algofinserve.recommendation.infra;

import com.angelbroking.smartapi.SmartConnect;
import com.angelbroking.smartapi.http.SessionExpiryHook;
import com.angelbroking.smartapi.models.TokenSet;
import com.angelbroking.smartapi.models.User;
import com.warrenstrange.googleauth.GoogleAuthenticator;

public final class AngelBrokerConnector {

  private AngelBrokerConnector() {}
  
  // Singleton pattern for SmartConnect instance
  private static SmartConnect smartConnect = null;
  private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

  public static synchronized SmartConnect getSmartConnectSession(AngelApiKey angelApiKey) {
    if (smartConnect == null) {
      smartConnect = new SmartConnect();
    }
    smartConnect.setApiKey(angelApiKey.getApiKey());

    smartConnect.setSessionExpiryHook(
        new SessionExpiryHook() {
          @Override
          public void sessionExpired() {
            System.out.println("Angel One session expired - attempting to regenerate");
            // In a real implementation, you might want to trigger a session refresh here
          }
        });
    
    String totp = getTotp(angelApiKey);
    if (smartConnect == null) {
      getSmartConnectSession(angelApiKey);
    }
    generateSession(smartConnect, angelApiKey.getClientId(), angelApiKey.getPassword(), totp);
    return smartConnect;
  }

  private static String getTotp(AngelApiKey angelApiKey) {
    int code = gAuth.getTotpPassword(angelApiKey.getTotp());
    return String.valueOf(code);
  }

  private void regenerateToken(SmartConnect smartConnect, User user) {
    TokenSet tokenSet =
        smartConnect.renewAccessToken(user.getAccessToken(), user.getRefreshToken());
    smartConnect.setAccessToken(tokenSet.getAccessToken());
  }

  private static void generateSession(
      SmartConnect smartConnect, String clientID, String password, String topt) {
    if (smartConnect != null) {
      try {
        System.out.println("Generating Angel One session for client: " + clientID);
        User user = smartConnect.generateSession(clientID, password, topt);
        if (user != null) {
          smartConnect.setAccessToken(user.getAccessToken());
          smartConnect.setUserId(user.getUserId());
          System.out.println("Angel One session generated successfully");
        } else {
          System.err.println("Failed to generate Angel One session - user is null");
        }
      } catch (Exception e) {
        System.err.println("Error generating Angel One session: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }
}
