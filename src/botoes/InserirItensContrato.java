package botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import com.sankhya.ce.jape.JapeHelper;

import javax.management.ObjectName;
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
                Object codIteImportacao = vo.asBigDecimalOrZero("CODIMPCONTPRO");
                Object itemInserido = vo.asString("ITEMINSERIDO");

                if (!itemInserido.equals("S")) {

                    DynamicVO contrato = JapeHelper.getVO("Contrato", "AD_CONTRATO = '" + vo.asString("NUMCONTRATO") + "'");

                    if (contrato != null) {
                        BigDecimal nroContrato = contrato.asBigDecimalOrZero("NUMCONTRATO");
                        BigDecimal codparc = contrato.asBigDecimalOrZero("CODPARC");

                        JapeHelper.CreateNewLine newProdServContrato = new JapeHelper.CreateNewLine("ProdutoServicoContrato");
                        newProdServContrato.set("NUMCONTRATO", nroContrato);
                        newProdServContrato.set("CODPROD", vo.asBigDecimalOrZero("CODPROD"));
                        newProdServContrato.set("AD_DESCRDET", vo.asString("AD_DESCRDET"));
                        newProdServContrato.set("NUMUSUARIOS", vo.asBigDecimalOrZero("NUMUSUARIOS"));
                        newProdServContrato.set("AD_CHAVELPU", vo.asString("CHAVE"));
                        newProdServContrato.set("AD_REGIONAL", vo.asString("REGIONAL"));
                        newProdServContrato.save();

                        JapeHelper.CreateNewLine newPreco = new JapeHelper.CreateNewLine("PrecoContrato");
                        newPreco.set("NUMCONTRATO", nroContrato);
                        newPreco.set("CODPROD", vo.asBigDecimalOrZero("CODPROD"));
                        newPreco.set("REFERENCIA", vo.asTimestamp("REFERENCIA"));
                        newPreco.set("VALOR", vo.asBigDecimalOrZero("VALOR"));
                        newPreco.save();

                        JapeHelper.CreateNewLine newOcorrencia = new JapeHelper.CreateNewLine("OcorrenciaContrato");
                        newOcorrencia.set("NUMCONTRATO", nroContrato);
                        newOcorrencia.set("CODPROD", vo.asBigDecimalOrZero("CODPROD"));
                        newOcorrencia.set("DTOCOR", new Timestamp(System.currentTimeMillis()));
                        newOcorrencia.set("CODOCOR", BigDecimal.ONE);
                        newOcorrencia.set("CODCONTATO", BigDecimal.ONE);
                        newOcorrencia.set("CODUSU", BigDecimal.ZERO);
                        newOcorrencia.set("DESCRICAO", "Ativação");
                        newOcorrencia.set("CODPARC", codparc);
                        newOcorrencia.save();

                        //Item iserido atualiza campo de verificacao na tabela
                        JapeSession.SessionHandle hnd = null;

                        try {
                            hnd = JapeSession.open();
                            JapeFactory.dao("AD_IMPORTITECONTPRO").
                                    prepareToUpdateByPK(codImportacao, codIteImportacao)
                                    .set("ITEMINSERIDO", "S")
                                    .update();
                        } catch (Exception e) {
                            MGEModelException.throwMe(e);
                        } finally {
                            JapeSession.close(hnd);
                        }


                    } else {
                        JapeSession.SessionHandle hnd = null;

                        try {
                            hnd = JapeSession.open();
                            JapeFactory.dao("AD_IMPORTITECONTPRO").
                                    prepareToUpdateByPK(codImportacao, codIteImportacao)
                                    .set("ERROR", "Contrato não encontrado.")
                                    .update();
                        } catch (Exception e) {
                            MGEModelException.throwMe(e);
                        } finally {
                            JapeSession.close(hnd);
                        }
                    }
                }
            }
        }

        contextoAcao.setMensagemRetorno("Itens criados com sucesso! ");
    }

}
