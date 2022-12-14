package vn.projectLTW.controller.admin;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.List;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import vn.projectLTW.model.Category;
import vn.projectLTW.model.Product;
import vn.projectLTW.model.Seller;
import vn.projectLTW.service.ICategoryService;
import vn.projectLTW.service.IProductService;
import vn.projectLTW.service.ISellerService;
import vn.projectLTW.service.Impl.CategoryServiceImpl;
import vn.projectLTW.service.Impl.ProductServiceImpl;
import vn.projectLTW.service.Impl.SellerServiceImpl;
import vn.projectLTW.util.Constant;
import vn.projectLTW.util.UploadUtils;

@MultipartConfig(fileSizeThreshold = 1024*1024*10,//10MB
					maxFileSize = 1024*1024*50,//50MB
					maxRequestSize = 1024*1024*50 ) //50MB

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/admin/product", "/admin/product/create", "/admin/product/update", "/admin/product/edit",
		"/admin/product/delete", "/admin/product/reset" })
public class ProductController extends HttpServlet {
	ICategoryService categoryService=new CategoryServiceImpl();
	ISellerService sellerService=new SellerServiceImpl();
	IProductService productService=new ProductServiceImpl();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		String url = req.getRequestURL().toString();
		Product product = null;

		if (url.contains("delete")) {
			delete(req, resp);
			product = new Product();
			req.setAttribute("product", product);// ?????y d??? li???u l??n Views

		} else if (url.contains("edit")) {
			edit(req, resp);
		} else if (url.contains("reset")) {
			product = new Product();
			req.setAttribute("product", product);// ?????y d??? li???u l??n Views

		}
		req.setAttribute("tag", "pro");

		findAll(req, resp); // hi???n danh s??ch product trong model

		// chuy???n v??? Views
		RequestDispatcher dispacher = req.getRequestDispatcher("/views/admin/list-product.jsp");
		dispacher.forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		String url = req.getRequestURL().toString();
		Product product = null;

		if (url.contains("create")) {
			create(req, resp);

		} else if (url.contains("update")) {
			update(req, resp);
		} else if (url.contains("reset")) {
			product = new Product();
			req.setAttribute("product", product);

		}
		findAll(req, resp); // hi???n danh s??ch product trong model

		// chuy???n v??? Views
		RequestDispatcher dispacher = req.getRequestDispatcher("/views/admin/list-product.jsp");
		dispacher.forward(req, resp);
	}

	private void findAll(HttpServletRequest req, HttpServletResponse resp) {
		try {
			List<Category> categoryList = categoryService.findAll();// g???i h??m findAll trong service tr??? v??? ?????i t?????ng List<products>
			req.setAttribute("categoryList", categoryList);// ?????y ds l??n Views
			
			List<Seller> sellerList=sellerService.findAll();//l???y t???t c??? seller trogn b???ng seller
			req.setAttribute("sellerList", sellerList);
			
			List<Product> productList=productService.findAll();
			req.setAttribute("productList", productList);

		} catch (Exception e) {
			e.printStackTrace();
			req.setAttribute("error", "Error: " + e.getMessage());
		}
	}

	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String id = req.getParameter("productId");// l???y tham s??? t??? Views c?? name = productId

			// X??a ???nh c?? ??i
			Product oldProduct = productService.findOne(Integer.parseInt(id));
			if (oldProduct.getImages() != null) {
				String fileName = oldProduct.getImages();
				UploadUtils.deleteFile(fileName, "\\products\\");
			}
			productService.delete(Integer.parseInt(id)); // g???i h??m delete trong service ????? x??a product th??ng qua id
			req.setAttribute("message", "???? x??a th??nh c??ng");
		} catch (Exception e) {
			e.printStackTrace();
			req.setAttribute("error", "Error: " + e.getMessage());
		}
	}

	protected void edit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String id = req.getParameter("productId");// l???y tham s??? t??? Views c?? name = id
			Product product = productService.findOne(Integer.parseInt(id)); // g???i h??m findOne trong service ????? l???y 1 product th??ng
																	// qua id
			req.setAttribute("product", product); // ?????y ?????i t?????ng product l??n views

			List<Category> categoryList = categoryService.findAll();// g???i h??m findAll trong service tr??? v??? ?????i t?????ng List<products>
			req.setAttribute("categoryList", categoryList);// ?????y ds l??n Views
			
			List<Seller> sellerList=sellerService.findAll();//l???y t???t c??? seller trogn b???ng seller
			req.setAttribute("sellerList", sellerList);

		} catch (Exception e) {
			e.printStackTrace();
			req.setAttribute("error", "Error: " + e.getMessage());
		}
	}

	protected void create(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setCharacterEncoding("UTF-8");
			resp.setCharacterEncoding("UTF-8");
			//L???y d??? li???u t??? jsp b???ng BeanUtils
			Product product=new Product();
			BeanUtils.populate(product, req.getParameterMap());
			// x??? l?? h??nh ???nh
			if(req.getPart("images").getSize()!=0) {
				//x??? l?? h??nh ???nh
				String fileName=""+System.currentTimeMillis();
				product.setImages(
						UploadUtils.processUpload("images", req, Constant.DIR + "\\products\\", fileName));
			}
			product.setCreateDate(new Date(System.currentTimeMillis()));

			
			product.setCategory(categoryService.findOne(product.getCategoryId()));
			product.setSeller(sellerService.findOne(product.getSellerId()));
			
			//g???i pt insert  trong service
			productService.insert(product);
			
			req.setAttribute("product", product);
			req.setAttribute("message", "???? th??m th??nh c??ng");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			req.setAttribute("error", "Error"+e.getMessage());
		}
		

	}

	protected void update(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setCharacterEncoding("UTF-8");
			resp.setCharacterEncoding("UTF-8");
			
			// L???y d??? li???u t??? JSP b???ng BeanUtils
			Product product=new Product();
			BeanUtils.populate(product, req.getParameterMap());
			// X??? l?? h??nh ???nh
			//Kh???i t???o DAO
			Product oldproduct=productService.findOne(product.getProductId());
//			System.out.println(oldproduct.getImages());
			if(req.getPart("images").getSize()==0) { 
				product.setImages(oldproduct.getImages());
			}else {
				if(oldproduct.getImages()!=null) {
					//x??a ???nh c?? ??i
					String fileName=oldproduct.getImages();
					UploadUtils.deleteFile(fileName, "\\product\\");
					//x??? l?? h??nh ???nh
					fileName=""+System.currentTimeMillis();
					product.setImages(UploadUtils.processUpload("images", req, Constant.DIR+"\\products\\", fileName));
				
				}
			}
			//L???y d??? li???u category,seller t??? b???ng Category v?? b???ng Seller
			product.setCategory(categoryService.findOne(product.getCategoryId()));
			product.setSeller(sellerService.findOne(product.getSellerId()));
			
			productService.update(product);
			
			//th??ng b??o 
			req.setAttribute("product", product);
			req.setAttribute("message", "C???p nh???t th??nh c??ng!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();	
			req.setAttribute("error", "Error: "+e.getMessage());
		}
	}
}





