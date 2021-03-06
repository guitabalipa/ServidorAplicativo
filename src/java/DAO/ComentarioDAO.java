/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Comentario;
import model.Pessoa;

/**
 *
 * @author Guilherme
 */
public class ComentarioDAO {

    private Connection con = null;
    PreparedStatement ptmt = null;
    ResultSet resultSet = null;

    public boolean inserirComentario(Comentario comentario) throws SQLException {

        String insereComentario = "INSERT INTO comentario( descricao, modificado, fkpessoa, fkidcomentario_dependente, data_criacao, data_modificacao ) VALUES( ?,?,?,?,?,? )";
        String insereRelacao = "INSERT INTO relacao( identidade, tabela_entidade, idrelacionada, tabela_relacionada ) VALUES( ?,?,?,? )";
        boolean result = false;

        try {
            con = ConnectionFactory.getConnection();
            con.setAutoCommit(false);
            ptmt = con.prepareStatement(insereComentario, Statement.RETURN_GENERATED_KEYS);
            ptmt.setString(1, comentario.getDescricao());
            ptmt.setInt(2, 0);
            ptmt.setInt(3, comentario.getPessoaid());
            ptmt.setInt(4, comentario.getComentarioDependenteid());
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
            ptmt.setString(5, dateFormat.format(date));
            ptmt.setString(6, dateFormat.format(date));

            ptmt.executeUpdate();
            resultSet = ptmt.getGeneratedKeys();
            resultSet.next();
            int idComentario = resultSet.getInt(1);

            //con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(insereRelacao);
            ptmt.setInt(1, comentario.getComentadoid());
            ptmt.setString(2, comentario.getTipoComentado());
            ptmt.setInt(3, idComentario);
            ptmt.setString(4, "comentario");

            ptmt.execute();
            con.commit();
            result = true;

        } catch (SQLException ex) {
            con.rollback();
            result = false;
            throw new RuntimeException("Erro ao inserir comentario no banco de dados. " + ex);

        } finally {
            ptmt.close();
            return result;
        }
    }

    public void atualizarComentario(Comentario comentario) throws SQLException {

        String alteraEmpresa = "UPDATE comentario SET descricao = ?, modificado = ? WHERE idcomentario = ?";

        try {
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(alteraEmpresa);
            ptmt.setString(1, comentario.getDescricao());
            ptmt.setInt(2, comentario.getComentadoid());

            ptmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao atualizar comentario no banco de dados. " + ex);
        } finally {
            ptmt.close();
        }
    }

    public void deletarComentario() {

    }
    
    public List<Comentario> getComentariosByIdEmpresa(int idEmpresa) throws SQLException{
/*        String sql = "SELECT distinct * FROM comentario";;
                   + " INNER JOIN relacao ON tabela_relacionada = 'comentario' AND  "
                   + "tabela_entidade = 'empresa' AND "
                   + "identidade = ? AND "
                   + "idcomentario = idrelacionada "
                   + "order by idcomentario DESC "
                   + "limit 3"; 
*/
        String sql = "SELECT distinct comentario.*, pessoa.* FROM comentario "
                + "INNER JOIN relacao ON relacao.idrelacionada = comentario.idcomentario AND relacao.tabela_relacionada = 'comentario'  "
                + "INNER JOIN pessoa ON fkpessoa = idpessoa "
                + "where tabela_entidade = 'empresa' and relacao.identidade = ? AND fkidcomentario_dependente = 0 "
                + "order by idcomentario DESC "
                + "LIMIT 3";
        List<Comentario> comentarios = new ArrayList<Comentario>();
        try{
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(sql);
            ptmt.setInt(1, idEmpresa);
            resultSet = ptmt.executeQuery();
            while(resultSet.next()){
                Comentario comentario = new Comentario();
                Pessoa pessoa = new Pessoa();
                comentario.setComentarioid(resultSet.getInt("idcomentario"));
                comentario.setComentadoid(idEmpresa);
                comentario.setComentarioDependenteid(resultSet.getInt("fkidcomentario_dependente"));
                comentario.setDescricao(resultSet.getString("descricao"));
                comentario.setModificado(resultSet.getInt("modificado"));
                comentario.setPessoaid(resultSet.getInt("fkpessoa"));
                comentario.setData_criacao(resultSet.getDate("data_criacao"));
                comentario.setData_modificacao(resultSet.getDate("data_modificacao"));
                    pessoa.setNome(resultSet.getString("nome"));
                    pessoa.setSobrenome(resultSet.getString("sobrenome"));
                comentario.setPessoa(pessoa);
                comentarios.add(comentario);
            }
            return comentarios;

            
        } catch (SQLException ex){
            throw new RuntimeException("Erro ao buscar comentario no banco de dados. " + ex);
            
        }finally{
            ptmt.close();
        }
        
    }

    public List<Comentario> getComentariosByIdEmpresaSemLimite(Integer idEmpresa) throws SQLException {
         String sql = "SELECT DISTINCT comentario.*, pessoa.*, "
                        + "(SELECT COUNT(*) FROM comentario cd WHERE cd.fkidcomentario_dependente = comentario.idcomentario) AS cont_dependentes "
                        + "FROM comentario "
                        + "INNER JOIN relacao ON relacao.idrelacionada = comentario.idcomentario AND relacao.tabela_relacionada = 'comentario' "
                        + "INNER JOIN pessoa ON fkpessoa = idpessoa "
                        + "WHERE tabela_entidade = 'empresa' AND relacao.identidade = ? AND fkidcomentario_dependente = 0 "
                        + "ORDER BY idcomentario DESC";

        List<Comentario> comentarios = new ArrayList<Comentario>();
        try{
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(sql);
            ptmt.setInt(1, idEmpresa);
            resultSet = ptmt.executeQuery();
            while(resultSet.next()){
                Comentario comentario = new Comentario();
                Pessoa pessoa = new Pessoa();
                comentario.setComentarioid(resultSet.getInt("idcomentario"));
                comentario.setComentadoid(idEmpresa);
                comentario.setComentarioDependenteid(resultSet.getInt("fkidcomentario_dependente"));
                comentario.setDescricao(resultSet.getString("descricao"));
                comentario.setModificado(resultSet.getInt("modificado"));
                comentario.setPessoaid(resultSet.getInt("fkpessoa"));
                comentario.setData_criacao(resultSet.getDate("data_criacao"));
                comentario.setData_modificacao(resultSet.getDate("data_modificacao"));
                comentario.setCont_comentarios_dependentes(resultSet.getInt("cont_dependentes"));
                    pessoa.setPessoaid(resultSet.getInt("idpessoa"));
                    pessoa.setNome(resultSet.getString("nome"));
                    pessoa.setSobrenome(resultSet.getString("sobrenome"));
                comentario.setPessoa(pessoa);
                comentarios.add(comentario);
            }
            return comentarios;

            
        } catch (SQLException ex){
            throw new RuntimeException("Erro ao buscar comentario no banco de dados. " + ex);
            
        }finally{
            ptmt.close();
        }
    }
    
    public List<Comentario> pegarComentariosDependentes(Integer idComentario) throws Exception {
        
        String sql = "SELECT distinct comentario.*, pessoa.* FROM comentario "
                        + "INNER JOIN pessoa ON fkpessoa = idpessoa "
                        + "where fkidcomentario_dependente = ?";
        
        List<Comentario> comentarios = new ArrayList<Comentario>();
        try{
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(sql);
            ptmt.setInt(1, idComentario);
            resultSet = ptmt.executeQuery();
            while(resultSet.next()){
                Comentario comentario = new Comentario();
                Pessoa pessoa = new Pessoa();
                comentario.setComentarioid(resultSet.getInt("idcomentario"));
                comentario.setComentarioDependenteid(resultSet.getInt("fkidcomentario_dependente"));
                comentario.setDescricao(resultSet.getString("descricao"));
                comentario.setData_criacao(resultSet.getDate("data_criacao"));
                comentario.setData_modificacao(resultSet.getDate("data_modificacao"));
                    pessoa.setPessoaid(resultSet.getInt("idpessoa"));
                    pessoa.setNome(resultSet.getString("nome"));
                    pessoa.setSobrenome(resultSet.getString("sobrenome"));
                comentario.setPessoa(pessoa);
                comentarios.add(comentario);
            }
            return comentarios;

            
        } catch (SQLException ex){
            throw new RuntimeException("Erro ao buscar comentario no banco de dados. " + ex);
            
        }finally{
            ptmt.close();
        }
    }
    
    public void excluirComentario(Integer idComentario) throws Exception {
        
        String sql  = "DELETE FROM comentario WHERE idcomentario = ?";
        String sql2 = "DELETE FROM comentario WHERE fkidcomentario_dependente = ?";
        
        try{
            con = ConnectionFactory.getConnection();
            ptmt = con.prepareStatement(sql);
            ptmt.setInt(1, idComentario);
            ptmt.execute();
            
            ptmt = con.prepareStatement(sql2);
            ptmt.setInt(1, idComentario);
            ptmt.execute();
            
        } catch (SQLException ex){
            con.rollback();
            throw new RuntimeException("Erro ao excluir comentario no banco de dados. " + ex);
        }finally{
            ptmt.close();
        }
    }
}
