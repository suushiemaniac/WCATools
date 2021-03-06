package com.suushiemaniac.cubing.wca.result;

import com.suushiemaniac.cubing.wca.comp.Competition;
import com.suushiemaniac.cubing.wca.comp.Event;
import com.suushiemaniac.cubing.wca.comp.Round;
import com.suushiemaniac.cubing.wca.person.Person;
import com.suushiemaniac.cubing.wca.time.Format;
import com.suushiemaniac.cubing.wca.util.ArrayUtils;
import com.suushiemaniac.cubing.wca.util.WcaDatabase;
import com.suushiemaniac.cubing.wca.util.globe.Continent;
import com.suushiemaniac.cubing.wca.util.globe.Country;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Result {
    private static Result[] forQuery(PreparedStatement stat) {
        try {
            WcaDatabase db = WcaDatabase.inst();

            ResultSet res = db.query(stat);
            List<Result> resultList = new ArrayList<>();

            while (res.next()) {
                resultList.add(Result.fromPointedResult(res));
            }

            return resultList.toArray(new Result[resultList.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result fromPointedResult(ResultSet res) {
        try {
            return new Result(
				res.getString("competitionId"),
				res.getString("eventId"),
				res.getString("roundId"),
				res.getString("personName"),
				res.getString("personId"),
				res.getString("personCountryId"),
				res.getString("formatId"),
				res.getString("regionalSingleRecord"),
				res.getString("regionalAverageRecord"),
				res.getInt("pos"),
				res.getInt("best"),
				res.getInt("average"),
				new int[]{
					res.getInt("value1"),
					res.getInt("value2"),
					res.getInt("value3"),
					res.getInt("value4"),
					res.getInt("value5")
				}
			);
        } catch (SQLException e) {
            e.printStackTrace();
        	return null;
        }
    }

    public static Result[] forPerson(Person person) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE personId = ?");
            stat.setString(1, person.getId());

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] forCompetition(Competition competition) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE competitionId = ?");
            stat.setString(1, competition.getId());

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] wrForEvent(Event event) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE eventId = ? AND (regionalSingleRecord = ? OR regionalAverageRecord = ?)");
            stat.setString(1, event.getId());
            stat.setString(2, "WR");
            stat.setString(3, "WR");

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] crForEvent(Event event, Continent continent) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE eventId = ? AND (regionalSingleRecord = ? OR regionalAverageRecord = ?)");
            stat.setString(1, event.getId());
            stat.setString(2, continent.getRecordName());
            stat.setString(3, continent.getRecordName());

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] nrForEvent(Event event, Country country) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE eventId = ? AND personCountryId = ? AND (regionalSingleRecord = ? OR regionalAverageRecord = ?)");
            stat.setString(1, event.getId());
            stat.setString(2, country.getId());
            stat.setString(3, "NR");
            stat.setString(4, "NR");

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] singlePbsForPersonForEvent(Person person, Event event) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE personId = ? AND eventId = ? AND best > 0 ORDER BY best ASC LIMIT 1");
            stat.setString(1, person.getId());
            stat.setString(2, event.getId());

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] singlePbsForPerson(Person person) {
        Result[] allPbs = new Result[0];

        for (Event event : Event.listInst()) {
            allPbs = ArrayUtils.merge(allPbs, singlePbsForPersonForEvent(person, event));
        }

        return allPbs;
    }

    public static Result[] averagePbsForPersonForEvent(Person person, Event event) {
        try {
            WcaDatabase db = WcaDatabase.inst();
            PreparedStatement stat = db.prepareStatement("SELECT * FROM Results WHERE personId = ? AND eventId = ? AND average > 0 ORDER BY average ASC LIMIT 1");
            stat.setString(1, person.getId());
            stat.setString(2, event.getId());

            return forQuery(stat);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result[] averagePbsForPerson(Person person) {
        Result[] allPbs = new Result[0];

        for (Event event : Event.listInst()) {
            allPbs = ArrayUtils.merge(allPbs, averagePbsForPersonForEvent(person, event));
        }

        return allPbs;
    }

    //Database values
    private String competitionId;
    private String eventId;
    private String roundId;
    private String personName;
    private String personId;
    private String personCountryId;
    private String formatId;
    private String regionalSingleRecord;
    private String regionalAverageRecord;

    private int pos;
    private int best;
    private int average;
    private int[] values;

    //Derived properties
    private Competition competition;

    private Event event;

    private Round round;

    private Person person;

    private Format format;

    private Solve bestSolve;
    private Solve averageSolve;
    private Solve[] solves;

    public Result(String competitionId, String eventId, String roundId, String personName, String personId, String personCountryId, String formatId, String regionalSingleRecord, String regionalAverageRecord, int pos, int best, int average, int[] values) {
        this.competitionId = competitionId;
        this.eventId = eventId;
        this.roundId = roundId;
        this.personName = personName;
        this.personId = personId;
        this.personCountryId = personCountryId;
        this.formatId = formatId;
        this.regionalSingleRecord = regionalSingleRecord;
        this.regionalAverageRecord = regionalAverageRecord;
        this.pos = pos;
        this.best = best;
        this.average = average;
        this.values = values;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public Competition getCompetition() {
        if (competition == null || !competition.getId().equals(competitionId)) {
            competition = Competition.fromId(competitionId);
        }

        return competition;
    }

    public String getEventId() {
        return eventId;
    }

    public Event getEvent() {
        if (event == null || !event.getId().equals(eventId)) {
            event = Event.fromID(eventId);
        }

        return event;
    }

    public String getRoundId() {
        return roundId;
    }

    public Round getRound() {
        if (round == null || !round.getId().equals(roundId)) {
            round = Round.fromID(roundId);
        }

        return round;
    }

    public String getPersonId() {
        return personId;
    }

    public Person getPerson() {
        if (person == null || !person.getWcaId().toFormatString().equals(personId)) {
            person = Person.fromID(personId);
        }

        return person;
    }

    public String getPersonName() {
        return personName;
    }

    public String getPersonCountryId() {
        return personCountryId;
    }

    public String getFormatId() {
        return formatId;
    }

    public Format getFormat() {
        if (format == null || !format.getId().equals(formatId)) {
            format = Format.fromID(formatId);
        }

        return format;
    }

    public String getRegionalSingleRecord() {
        return regionalSingleRecord;
    }

    public String getRegionalAverageRecord() {
        return regionalAverageRecord;
    }

    public int getPos() {
        return pos;
    }

    public int getBest() {
        return best;
    }

    public Solve getBestSolve() {
        if (bestSolve == null) {
            bestSolve = new Solve(best, this.getEvent());
        }

        return bestSolve;
    }

    public int getAverage() {
        return average;
    }

    public Solve getAverageSolve() {
        if (averageSolve == null) {
            averageSolve = new Solve(average, this.getEvent(), true);
        }

        return averageSolve;
    }

    public int[] getValues() {
        return values;
    }

    public Solve[] getSolves() {
        if (solves == null || solves.length <= 0) {
            solves = new Solve[values.length];

            for (int i = 0; i < values.length; i++) {
                solves[i] = new Solve(values[i], this.getEvent());
            }
        }

        return solves;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setPersonCountryId(String personCountryId) {
        this.personCountryId = personCountryId;
    }

    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    public void setRegionalSingleRecord(String regionalSingleRecord) {
        this.regionalSingleRecord = regionalSingleRecord;
    }

    public void setRegionalAverageRecord(String regionalAverageRecord) {
        this.regionalAverageRecord = regionalAverageRecord;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setBest(int best) {
        this.best = best;
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public void setValues(int[] values) {
        this.values = values;
    }
}