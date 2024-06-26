package botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import com.sankhya.ce.jape.JapeHelper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

public class InserirItensContrato implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        Registro[] linhas = contextoAcao.getLinhas();

        for (Registro linha : linhas) {
            Object codImportacao = linha.getCampo("CODIMPITECONT");

            Collection<DynamicVO> itensContratos = JapeHelper.getVOs("AD_IMPORTITECONTPRO", "CODIMPITECONT = " + codImportacao);
            for (DynamicVO vo : itensContratos) {
                DynamicVO contrato = JapeHelper.getVO("Contrato", "NUMCONTRATO = " + vo.asBigDecimalOrZero("NUMCONTRATO").toString());
                if (contrato == null) throw new MGEModelException("Contrato não encontrado.");

                BigDecimal codparc = contrato.asBigDecimalOrZero("CODPARC");

                JapeHelper.CreateNewLine newProdServContrato = new JapeHelper.CreateNewLine("ProdutoServicoContrato");
                newProdServContrato.set("NUMCONTRATO", vo.asBigDecimalOrZero("NUMCONTRATO"));
                newProdServContrato.set("CODPROD", vo.asBigDecimalOrZero("CODPROD"));
                newProdServContrato.set("NUMUSUARIOS", vo.asBigDecimalOrZero("NUMUSUARIOS"));
                newProdServContrato.set("AD_CHAVELPU", vo.asString("CHAVELPU"));
                newProdServContrato.set("AD_REGIONAL", vo.asString("REGIONAL"));
                newProdServContrato.save();

                JapeHelper.CreateNewLine newPreco = new JapeHelper.CreateNewLine("PrecoContrato");
                newPreco.set("NUMCONTRATO", vo.asBigDecimalOrZero("NUMCONTRATO"));
                newPreco.set("CODPROD", vo.asBigDecimalOrZero("CODPROD"));
                newPreco.set("REFERENCIA", vo.asTimestamp("REFERENCIA"));
                newPreco.set("VALOR", vo.asBigDecimalOrZero("VALOR"));
                newPreco.save();

                JapeHelper.CreateNewLine newOcorrencia = new JapeHelper.CreateNewLine("OcorrenciaContrato");
                newOcorrencia.set("NUMCONTRATO", vo.asBigDecimalOrZero("NUMCONTRATO"));
                newOcorrencia.set("CODPROD", vo.asBigDecimalOrZero("CODPROD"));
                newOcorrencia.set("DTOCOR", new Timestamp(System.currentTimeMillis()));
                newOcorrencia.set("CODOCOR", BigDecimal.ONE);
                newOcorrencia.set("CODCONTATO", BigDecimal.ONE);
                newOcorrencia.set("CODUSU", BigDecimal.ZERO);
                newOcorrencia.set("DESCRICAO", "Ativação");
                newOcorrencia.set("CODPARC", codparc);
                newOcorrencia.save();

            }
        }

        contextoAcao.setMensagemRetorno("Itens criados com sucesso! ");
    }

}
