package org.eclipse.daanse.olap.function.def.periodstodate;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.api.LevelCalc;
import org.eclipse.daanse.olap.calc.api.MemberCalc;
import org.eclipse.daanse.olap.calc.api.todo.TupleList;

import mondrian.calc.impl.AbstractListCalc;
import mondrian.calc.impl.UnaryTupleList;
import mondrian.olap.fun.FunUtil;
import mondrian.rolap.RolapHierarchy;

public class PeriodsToDateCalc extends AbstractListCalc {

    private static final String TIMING_NAME = PeriodsToDateFunDef.class.getSimpleName();

    private final RolapHierarchy timeHierarchy;

    public PeriodsToDateCalc(Type type, LevelCalc levelCalc, MemberCalc memberCalc,
            final RolapHierarchy timeHierarchy) {
        super(type, levelCalc, memberCalc);
        this.timeHierarchy = timeHierarchy;
    }

    @Override
    public TupleList evaluateList(Evaluator evaluator) {
        LevelCalc levelCalc = getChildCalc(0, LevelCalc.class);
        MemberCalc memberCalc = getChildCalc(1, MemberCalc.class);
        evaluator.getTiming().markStart(TIMING_NAME);
        try {
            final Member member;
            final Level level;
            if (levelCalc == null) {
                member = evaluator.getContext(timeHierarchy);
                level = member.getLevel().getParentLevel();
            } else {
                level = levelCalc.evaluate(evaluator);
                if (memberCalc == null) {
                    member = evaluator.getContext(level.getHierarchy());
                } else {
                    member = memberCalc.evaluate(evaluator);
                }
            }
            return new UnaryTupleList(FunUtil.periodsToDate(evaluator, level, member));
        } finally {
            evaluator.getTiming().markEnd(TIMING_NAME);
        }
    }

    @Override
    public boolean dependsOn(Hierarchy hierarchy) {
        LevelCalc levelCalc = getChildCalc(0, LevelCalc.class);
        MemberCalc memberCalc = getChildCalc(1, MemberCalc.class);
        if (super.dependsOn(hierarchy)) {
            return true;
        }
        if (memberCalc != null) {
            return false;
        } else if (levelCalc != null) {
            return levelCalc.getType().usesHierarchy(hierarchy, true);
        } else {
            return hierarchy == timeHierarchy;
        }
    }

}