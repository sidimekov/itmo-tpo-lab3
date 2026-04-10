package utils;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class AuthSetup {
    public static final String BASE_URL = "https://mix.com/";
    public static final String AUTH_COOKIE_NAME = "intoprd";
    public static final String AUTH_COOKIE_ENV = "MIX_AUTH_COOKIE";
    public static final String AUTH_COOKIE_UID = "MIX_UID_COOKIE";

    private AuthSetup() {
    }

    public static WebDriver createAuthorizedDriver() {
        WebDriver driver = new FirefoxDriver();
        driver.manage().window().maximize();

        String authCookieValue = System.getenv(AUTH_COOKIE_ENV);
        String envMixUid = System.getenv(AUTH_COOKIE_UID);
        if (authCookieValue == null || authCookieValue.isBlank()) {
            driver.quit();
            throw new IllegalStateException("MIX_AUTH_COOKIE не поставлен в окружении. закинь туда параметр cookie под названием intoprd когда зашёл в свой аккаунт");
        }

        driver.get(BASE_URL);

        driver.manage().addCookie(
                new Cookie.Builder(AUTH_COOKIE_NAME, authCookieValue)
                        .domain("mix.com")
                        .path("/")
                        .isSecure(true)
                        .build()
        );
        driver.manage().addCookie(
                new Cookie.Builder("token", authCookieValue)
                        .domain("mix.com")
                        .path("/")
                        .isSecure(true)
                        .build()
        );

        driver.manage().addCookie(new Cookie.Builder("mix_uid", envMixUid)
                .domain("mix.com")
                .path("/")
                .isSecure(true)
                .build());

        driver.manage().addCookie(new Cookie.Builder("mix_i", "true")
                .domain("mix.com")
                .path("/")
                .isSecure(true)
                .build());

        driver.navigate().refresh();
        waitForPageLoaded(driver);

        return driver;
    }

    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }

    public static void waitForPageLoaded(WebDriver driver) {
        for (int i = 0; i < 30; i++) {
            Object readyState = ((JavascriptExecutor) driver).executeScript("return document.readyState");
            if ("complete".equals(String.valueOf(readyState))) {
                return;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Оборвана загрузка страницы", e);
            }
        }

        throw new RuntimeException("Не загрузилось");
    }
}