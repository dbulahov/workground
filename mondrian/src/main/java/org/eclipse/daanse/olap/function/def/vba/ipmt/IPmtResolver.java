/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.olap.function.def.vba.ipmt;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class IPmtResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("IPmt");
    private static String SIGNATURE = "IPmt(rate, per, nper, pv[, fv[, type]])";
    private static String DESCRIPTION = """
            Returns a Double specifying the interest payment for a given period
            of an annuity based on periodic, fixed payments and a fixed
            interest rate.""";

    private static FunctionParameterR[] p1 = { new FunctionParameterR(DataType.NUMERIC, "Rate"),
            new FunctionParameterR(DataType.NUMERIC, "Per"), new FunctionParameterR(DataType.NUMERIC, "NPer"),
            new FunctionParameterR(DataType.NUMERIC, "Pv") };
    private static FunctionParameterR[] p2 = { new FunctionParameterR(DataType.NUMERIC, "Rate"),
            new FunctionParameterR(DataType.NUMERIC, "Per"), new FunctionParameterR(DataType.NUMERIC, "NPer"),
            new FunctionParameterR(DataType.NUMERIC, "Pv"), new FunctionParameterR(DataType.NUMERIC, "Fv") };
    private static FunctionParameterR[] p3 = { new FunctionParameterR(DataType.NUMERIC, "Rate"),
            new FunctionParameterR(DataType.NUMERIC, "Per"), new FunctionParameterR(DataType.NUMERIC, "NPer"),
            new FunctionParameterR(DataType.NUMERIC, "Pv"), new FunctionParameterR(DataType.NUMERIC, "Fv"),
            new FunctionParameterR(DataType.LOGICAL, "due") };

    private static FunctionMetaData functionMetaData1 = new FunctionMetaDataR(atom, DESCRIPTION, SIGNATURE,
            DataType.NUMERIC, p1);
    private static FunctionMetaData functionMetaData2 = new FunctionMetaDataR(atom, DESCRIPTION, SIGNATURE,
            DataType.NUMERIC, p2);
    private static FunctionMetaData functionMetaData3 = new FunctionMetaDataR(atom, DESCRIPTION, SIGNATURE,
            DataType.NUMERIC, p3);

    public IPmtResolver() {
        super(List.of(new IPmtFunDef(functionMetaData1), new IPmtFunDef(functionMetaData2),
                new IPmtFunDef(functionMetaData3)));
    }
}