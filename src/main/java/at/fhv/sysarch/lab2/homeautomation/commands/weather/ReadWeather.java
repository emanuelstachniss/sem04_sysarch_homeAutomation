package at.fhv.sysarch.lab2.homeautomation.commands.weather;

public class ReadWeather implements WeatherCommand {
    private final WeatherTypes weather;

    public ReadWeather(WeatherTypes weather) {
        this.weather = weather;
    }

    public WeatherTypes getWeather() {
        return weather;
    }
}
