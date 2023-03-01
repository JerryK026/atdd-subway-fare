package nextstep.subway.domain;

import nextstep.subway.domain.fare.DiscountPolicy;
import nextstep.subway.domain.fare.Fare;

import java.util.List;

public class Path {
    private final Sections sections;
    private final Fare fare;

    public Path(Sections sections) {
        this(sections, DiscountPolicy.GENERAL);
    }

    public Path(Sections sections, DiscountPolicy policy) {
        this.sections = sections;
        this.fare = new Fare(sections, policy);
    }

    public int distance() {
        return sections.sumByCondition(Section::getDistance);
    }

    public int duration() {
        return sections.sumByCondition(Section::getDuration);
    }

    public int cost() {
        return fare.cost();
    }

    public List<Station> getStations() {
        return sections.getStations();
    }
}
