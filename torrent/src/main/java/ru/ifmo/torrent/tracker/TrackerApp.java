package ru.ifmo.torrent.tracker;

import ru.ifmo.torrent.util.TorrentException;

import java.util.Scanner;

public class TrackerApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Tracker tracker = new Tracker(TrackerConfig.TRACKER_PORT)) {
            System.out.println("enter 'stop' to stop tracker");
            tracker.run();
            while (scanner.hasNext()) {
                String command = scanner.next();
                if (command.equals("stop")) {
                    System.out.println("shutting down tracker");
                    break;
                }
            }
        } catch (TorrentException e) {
            System.out.println(e.getMassage());
            if(e.getException() != null) e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
