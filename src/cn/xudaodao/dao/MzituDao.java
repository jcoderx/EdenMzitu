package cn.xudaodao.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cn.xudaodao.util.MD5Utils;
import cn.xudaodao.util.UUIDUtils;

public class MzituDao {
	// UUID 标题 发布时间 类型 tag，缩略图url，所在页面url，总图片数量

	// id title publish_time category tag thumbs_url under_page_rul count

	// UUID url 所属标题（1-N,标题对应的UUID，所属页面url ，浏览次数

	// id pic_url under_page_url pv under_title_id

	// CREATE TABLE `tb_mzitu_pic` (
	// `id` varchar(200) NOT NULL,
	// `title` varchar(200) NOT NULL,
	// `publish_time` varchar(50) DEFAULT NULL,
	// `category` varchar(50) DEFAULT NULL,
	// `tag` varchar(200) DEFAULT NULL,
	// `thumbs_url` varchar(200) DEFAULT NULL,
	// `under_page_rul` varchar(200) DEFAULT NULL,
	// `count` int(11) DEFAULT NULL,
	// PRIMARY KEY (`id`)
	// ) ENGINE=InnoDB DEFAULT CHARSET=latin1;

	// CREATE TABLE `tb_mzitu_url` (
	// `id` varchar(200) NOT NULL,
	// `pic_url` varchar(200) DEFAULT NULL,
	// `under_page_url` varchar(200) DEFAULT NULL,
	// `pv` varchar(30) DEFAULT NULL,
	// `under_title_id` varchar(200) DEFAULT NULL,
	// `pic_index` int(11) DEFAULT NULL,
	// PRIMARY KEY (`id`)
	// ) ENGINE=InnoDB DEFAULT CHARSET=latin1;

	public static void updatePictureGroup(String groupUrl, String title, String publish_time, String category,
			int count) {
		String pictureGroupId = getPictureGroupId(groupUrl);
		String sql = "update tb_mzitu_pic set title=?,publish_time=?,category=?,count=? where id=?";
		PreparedStatement pstmt;
		try {
			Connection conn = DbManager.getInstance().getConnection();
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, title);
			pstmt.setString(2, publish_time);
			pstmt.setString(3, category);
			pstmt.setInt(4, count);
			pstmt.setString(5, pictureGroupId);
			int i = pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertPictureUrl(String groupUrl,String pic_url, String under_page_url, String pv, int pic_index) {
		String under_title_id = getPictureGroupId(groupUrl);
		String sql = "insert into tb_mzitu_url(id,pic_url,under_page_url,pv,under_title_id,pic_index) values(?,?,?,?,?,?)";
		PreparedStatement pstmt;
		try {
			Connection conn = DbManager.getInstance().getConnection();
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, UUIDUtils.getUUID());
			pstmt.setString(2, pic_url);
			pstmt.setString(3, under_page_url);
			pstmt.setString(4, pv);
			pstmt.setString(5, under_title_id);
			pstmt.setInt(6, pic_index);
			int i = pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getPictureGroupIdByGroupUrl(String groupUrl) {
		String md5 = MD5Utils.MD5(groupUrl);
		String sql = "select * from tb_mzitu_pic where id='" + md5 + "'";
		try {
			Connection conn = DbManager.getInstance().getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				String id = rs.getString("id");
				return id;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getPictureGroupId(String groupUrl) {
		String pictureGroupId = getPictureGroupIdByGroupUrl(groupUrl);
		if (pictureGroupId == null) {
			String sql = "insert into tb_mzitu_pic(id) values(?)";
			PreparedStatement pstmt;
			try {
				Connection conn = DbManager.getInstance().getConnection();
				pstmt = (PreparedStatement) conn.prepareStatement(sql);
				String id = MD5Utils.MD5(groupUrl);
				pstmt.setString(1, id);
				int i = pstmt.executeUpdate();
				pstmt.close();
				pictureGroupId = id;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pictureGroupId;
	}
}
