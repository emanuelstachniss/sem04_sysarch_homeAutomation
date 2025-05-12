package at.fhv.sysarch.lab2.homeautomation.commands.mediaStation;

public record MediaPlayer(String movieTitle, MediaType action) implements MediaCommand {
}
