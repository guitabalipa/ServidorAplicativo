/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Guilherme
 */
public class ProdutoDAO {
    
    private Connection con = null;
    PreparedStatement ptmt = null;
    ResultSet resultSet    = null;
    
    public Entidade cadastrarProduto(Produto prod, int pessoaid) throws SQLException{
        
        String cadastrarEmpresa  = "INSERT INTO produto(nomeproduto, descricao, preco, fkcategoria, fktipo_culinaria) values(?,?,?,?,?);";
        String cadastrarEntidade = "INSERT INTO entidade(identidade_criada, deletado, tabela, idresponsavel, data_criacao, data_modificacao, idcriador) values(?,?,?,?,?,?,?);";
        String cadastrarRelacao  = "INSERT INTO relacao(identidade, tabela_entidade, idrelacionada, tabela_relacionada) values(?,?,?,?);";
        
        
        try {
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(cadastrarEmpresa, Statement.RETURN_GENERATED_KEYS);
                ptmt.setString(1, prod.getNomeProduto());
                ptmt.setString(2, prod.getDescricao());
                ptmt.setDouble(3, prod.getPreco());
                ptmt.setInt(4, prod.getCategoria());
                ptmt.setInt(5, prod.getCulinaria());
            
            ptmt.executeUpdate();
            resultSet = ptmt.getGeneratedKeys();
            resultSet.next();
            int idProduto = resultSet.getInt(1);
            
            
            ptmt = con.prepareStatement(cadastrarEntidade, Statement.RETURN_GENERATED_KEYS);
                ptmt.setInt(1, idProduto);
                ptmt.setInt(2, 0);
                ptmt.setString(3, "produto");
                ptmt.setInt(4, pessoaid);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                ptmt.setString(5, dateFormat.format(date));
                ptmt.setString(6, dateFormat.format(date));
                ptmt.setInt(7, pessoaid);
            ptmt.executeUpdate();
            resultSet = ptmt.getGeneratedKeys();
            resultSet.next();
            Entidade entidade = new Entidade();
            entidade.setIdentidade(resultSet.getInt(1));
            entidade.setIdentidade_criada(idProduto);
            
            ptmt = con.prepareStatement(cadastrarRelacao);
                ptmt.setInt(1, prod.getEmpresaid());
                ptmt.setString(2, "empresa");
                ptmt.setInt(3, idProduto);
                ptmt.setString(4, "produto");
            ptmt.executeUpdate();
            
            return entidade;
            
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao inserir empresa no banco de dados. "+ex);
        } finally {
            ptmt.close();
        }
    }
    
    private Entidade setGetEntidade(ResultSet resultSet)throws SQLException{
        Entidade entidade = new Entidade();
        entidade.setData_criacao(resultSet.getDate("data_criacao"));
        entidade.setData_modificacao(resultSet.getDate("data_modificacao"));
        entidade.setDeletado(resultSet.getInt("deletado"));
        entidade.setIdcriador(resultSet.getInt("idcriador"));
        entidade.setIdentidade(resultSet.getInt("identidade"));
        entidade.setIdentidade_criada(resultSet.getInt("identidade_criada"));
        entidade.setIdresponsavel(resultSet.getInt("idresponsavel"));
        entidade.setTabela(resultSet.getString("tabela"));
        return entidade;
    }

    public Produto getProdutoById(int idProduto) throws SQLException {
        String sql = "SELECT DISTINCT produto.*, imagem.*, "
                + "	(SELECT COUNT(*) FROM comentario "
                + "		INNER JOIN relacao ON relacao.idrelacionada = comentario.idcomentario AND relacao.tabela_relacionada = 'comentario' "
                + "		WHERE relacao.identidade = produto.idproduto AND relacao.tabela_entidade = 'produto') AS qtdecomentarios, "
                + "(SELECT COUNT(*) FROM avaliacao WHERE produto.idproduto = avaliacao.idavaliado " 
                + "     AND avaliacao.tipoavaliacao = 'produto') AS qtdeavaliacoes, "
                
                + "(SELECT avg(avaliacao) FROM avaliacao "
                + "WHERE produto.idproduto = avaliacao.idavaliado AND avaliacao.tipoavaliacao = 'produto') AS media "
                
                + "FROM produto "
                + "INNER JOIN entidade ON produto.idproduto = entidade.identidade_criada AND entidade.deletado = 0  "
                + "LEFT JOIN relacao rp ON produto.idproduto = rp.idrelacionada AND rp.tabela_relacionada = 'produto'  "
                + "LEFT JOIN relacao ri ON ri.identidade = produto.idproduto AND ri.tabela_relacionada = 'imagem'  "
                + "LEFT JOIN imagem  ON imagem.idimagem = ri.idrelacionada  "
                + "WHERE idproduto = ?;";

        try {
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(sql);
                ptmt.setInt(1, idProduto);
                //ptmt.setInt(2, idProduto);
                
            resultSet = ptmt.executeQuery();
            if (resultSet.next()){
            Produto p = new Produto();
            p.setProdutoid(idProduto);
            p.setAvaliacaoGeral(resultSet.getInt("media"));

            p.setCategoria(resultSet.getInt("fkcategoria"));
            p.setPreco(resultSet.getDouble("preco"));
            
            Imagem imagem = new Imagem();
            imagem.setCaminho(resultSet.getString("caminho"));
            imagem.setDescricao(resultSet.getString("descricao"));
            imagem.setImagemid(resultSet.getInt("idimagem"));
            imagem.setItemid(idProduto);
            imagem.setNomeImagem(resultSet.getString("nomeimagem"));
            imagem.setTipoImagem(resultSet.getInt("fktipo_imagem"));
            
            p.setImagemPerfil(imagem);
            
            p.setNomeProduto(resultSet.getString("nomeproduto"));
            p.setQtdeAvaliacoes(resultSet.getInt("qtdeavaliacoes"));
            
            AvaliacaoDAO avaliacaoDao = new AvaliacaoDAO();
            p.setAvaliacoes(avaliacaoDao.getAvaliacoesByIdProduto(idProduto));
            return p;
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao recuperar o produto no banco de dados. "+ex);
        } finally {
            ptmt.close();
        }
    }
}
