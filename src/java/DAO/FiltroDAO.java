/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import model.Empresa;
import model.Endereco;
import model.Entidade;
import model.Filtro;
import model.Imagem;

/**
 *
 * @author DeusFelipe
 */
public class FiltroDAO {
    private Connection con = null;
    PreparedStatement ptmt = null;
    ResultSet resultSet    = null;
    /**************************************************************************************************************************
     * filtraRestaurante
     * 
     * ~Parametros de entradas~
     * # Objeto Filtro, cujo os parametros serão usados para determinar a query de filtragem
     * # Id ultimo restaurante da busca, ao se clicar em "mais" a a query é refeita, nao pegando os ids que já foram pegos
     * 
     * ~Funcao~
     * # Retornar uma lista de restaurantes com base nos filtros escolhidos
    **************************************************************************************************************************/
    public List<Empresa> filtraEmpresa(Filtro filtro) throws SQLException{
        String sqlFiltro = " SELECT DISTINCT "
                                  + " e.*, im.*, en.*, ent.*,  "
                                  + "(SELECT COUNT(*) FROM comentario "
                                  + "INNER JOIN relacao ON relacao.idrelacionada = comentario.idcomentario AND relacao.tabela_relacionada = 'comentario' "
                                  + "WHERE relacao.identidade = e.idempresa AND relacao.tabela_entidade = 'empresa') AS qtdecomentarios, "
                                  + "(SELECT COUNT(*) FROM avaliacao "
                                  +  "INNER JOIN relacao ON relacao.idrelacionada = avaliacao.idavaliacao AND relacao.tabela_relacionada = 'avaliacao' "
                                  + "WHERE relacao.identidade = e.idempresa AND relacao.tabela_entidade = 'empresa') AS qtdeavaliacoes, "
                                  + "(SELECT AVG(avaliacao) FROM avaliacao "
                                  + "WHERE avaliacao.idavaliado = e.idempresa) AS avaliacaogeral "
                                  + "FROM empresa e "
                
                               + "INNER JOIN entidade ent   ON e.idempresa = ent.identidade_criada AND ent.deletado = 0 AND ent.tabela = 'empresa' "
                               + "LEFT JOIN relacao rp      ON e.idempresa = rp.identidade AND rp.tabela_relacionada = 'produto'"
                               + "LEFT JOIN produto p       ON p.idproduto  = rp.idrelacionada "
                               + "LEFT JOIN categoria c    ON p.fkcategoria = c.idcategoria "
                               + "LEFT JOIN relacao ri      ON ri.identidade = e.idempresa AND ri.tabela_relacionada = 'imagem' "
                               + "LEFT JOIN relacao ren     ON ren.identidade = e.idempresa AND ren.tabela_relacionada = 'endereco' "
                               + "LEFT JOIN imagem  im      ON im.idimagem = ri.idrelacionada AND im.fktipo_imagem = 1 "
                               + "LEFT JOIN endereco en     ON en.idendereco = ren.idrelacionada ";
        String filtragem = "";
        
        if(filtro.getNomeempresa() != null && !filtro.getNomeempresa().isEmpty()){
            filtro.setNomeempresa("%"+filtro.getNomeempresa()+"%");
            filtragem = sePrimeiroElemento(filtragem);
            filtragem = filtragem+" e.nomeempresa LIKE \""+filtro.getNomeempresa()+"\" " ;
        }
//        if(filtro.nomeproduto != null && !filtro.nomeproduto.isEmpty()){
//            filtragem = sePrimeiroElemento(filtragem);
//            filtragem = filtragem+" p.nomeproduto = "+filtro.nomeproduto+ " " ;
//        }
//        if(filtro.precoMinimo > 0 && filtro.precoMinimo < filtro.precoMaximo){
//            filtragem = sePrimeiroElemento(filtragem);
//            filtragem = filtragem+" p.preco > "+filtro.precoMinimo+ " " ;
//        }
//        if(filtro.precoMaximo > 0 && filtro.precoMaximo > filtro.precoMinimo){
//            filtragem = sePrimeiroElemento(filtragem);
//            filtragem = filtragem+" p.preco < "+filtro.precoMaximo+ " " ;
//        }
//        if(filtro.nome_categoria != null  && !filtro.nome_categoria.isEmpty()){
//            filtragem = sePrimeiroElemento(filtragem);
//            filtragem = filtragem+" c.nome_categoria = "+filtro.nome_categoria+ " " ;
//        }
//        if(filtro.estado != null && !filtro.estado.isEmpty()){
//            filtragem = sePrimeiroElemento(filtragem);
//            filtragem = filtragem+" en.estado = \""+filtro.estado+ "\" " ;
//        }
        if((filtro.getCidade() != null) && (!filtro.hasCidades())){
            if(!"Todos".equals(filtro.getCidade())){
            filtragem = sePrimeiroElemento(filtragem);
            filtragem = filtragem+(" en.cidade = \""+filtro.getCidade()+ "\" ") ;
            }
        }
//        if(filtro.getBairro() != null && !filtro.getBairro().isEmpty()){
//            filtragem = sePrimeiroElemento(filtragem);
//            filtragem = filtragem+" en.bairro = \""+filtro.getBairro()+ "\" " ;
//        }
        //aqui verifica-se se o objeto filtro entrou em algum dos ifs acima, se sim, então remove-se o ultimo "and" da string
//        if(!filtragem.equals("")){
//            filtragem = filtragem.substring(0, filtragem.length() - 5);          
//        }
         filtragem += " GROUP BY e.idempresa ";
        //aqui coloca-se algum order by se houver
        if(filtro.isMaisComentado()){
            filtragem = filtragem+" order by e.vezesComentado DESC " ;
        }
        else if(filtro.isMaisBuscado()){
            filtragem = sePrimeiroElemento(filtragem);
            filtragem = filtragem+" order by e.vezesBuscado DESC " ;
        } 
        sqlFiltro = sqlFiltro+" "+filtragem+" ; ";
        
        
        try{
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(sqlFiltro);
            resultSet = ptmt.executeQuery();
            List<Empresa> empresas = new ArrayList<Empresa>();
            
            while (resultSet.next()) { 
                Empresa empresa = new Empresa();
                empresa.setEmpresaId(resultSet.getInt("idempresa"));
                empresa.setNomeEmpresa(resultSet.getString("nomeempresa"));
                empresa.setCnpj(resultSet.getString("cnpj"));
                empresa.setDescricao(resultSet.getString("descricao"));
                
                Entidade entidade = new Entidade();
                entidade.setIdentidade(resultSet.getInt("identidade"));
                entidade.setIdentidade_criada(resultSet.getInt("identidade_criada"));
                entidade.setDeletado(resultSet.getInt("deletado"));
                entidade.setTabela(resultSet.getString("tabela"));
                entidade.setIdresponsavel(resultSet.getInt("idresponsavel"));
                entidade.setIdcriador(resultSet.getInt("idcriador"));
                //entidade.setData_criacao(resultSet.getDate("data_criacao"));
                //entidade.setData_modificacao(resultSet.getDate("data_modificacao"));
                
                Imagem imagemPerfil = new Imagem();
                imagemPerfil.setImagemid(resultSet.getInt("idimagem"));
                imagemPerfil.setNomeImagem(resultSet.getString("nomeimagem"));
                imagemPerfil.setCaminho(resultSet.getString("caminho"));
                imagemPerfil.setDescricao(resultSet.getString("descricao"));
                imagemPerfil.setTipoImagem(resultSet.getInt("fktipo_imagem"));
                
                Endereco endereco = new Endereco();
                endereco.setEnderecoid(resultSet.getInt("idendereco"));
                endereco.setRua(resultSet.getString("rua"));
                endereco.setBairro(resultSet.getString("bairro"));
                endereco.setCep(resultSet.getString("cep"));
                endereco.setNumero(resultSet.getString("numero"));
                endereco.setComplemento(resultSet.getString("complemento"));
                endereco.setCidade(resultSet.getString("cidade"));
                endereco.setEstado(resultSet.getString("estado"));
                endereco.setPais(resultSet.getString("pais"));
                
                empresa.setImagemPerfil(imagemPerfil);
                empresa.setEndereco(endereco);
                empresa.setQtdeComentarios(resultSet.getInt("qtdecomentarios"));
                empresa.setQtdeComentarios(resultSet.getInt("qtdeavaliacoes"));
                empresa.setAvaliacaoNota(Math.round(resultSet.getInt("avaliacaogeral")));
                empresa.setEntidade(entidade);
                
                empresas.add(empresa);
            }
            return empresas;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao busca empresa no banco de dados. "+ex);
        } finally {
            ptmt.close();
        }
    }
    
    /**************************************************************************************************************************
     * sePrimeiroElemento
     * 
     * ~Parametros de entradas~
     * # String filtragem
     * 
     * ~Funcao~
     * # Definir se a String filtragem tera "where" ou "and" na sql
    **************************************************************************************************************************/
   
    public String sePrimeiroElemento(String filtragem){
        if (filtragem.equals("")){
            filtragem = " where ";
        }
        else{
            filtragem = filtragem+ " and ";
        }
        return filtragem;
    }
}
