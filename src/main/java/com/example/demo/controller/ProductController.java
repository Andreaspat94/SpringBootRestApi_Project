package com.example.demo.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.demo.model.Product;
import com.example.demo.service.ProductServiceImpl;
import com.example.demo.util.CustomErrorType;

	
@SuppressWarnings({ "unchecked", "rawtypes" })
@RestController
@RequestMapping("bundles/v1/admin/products")
public class ProductController {
	
	public static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	
	@Autowired
	ProductServiceImpl productService;	
	
	//Get all products ordered by product ID/product_code/price(ascending or descending)/name
	@GetMapping(path="/{param}")
	public ResponseEntity<List<Product>> getProducts(@PathVariable(name="param") String param,
			@RequestParam(name="asc",required=false) boolean asc) {
		
		//getProducts method decides in which order the server sends back the data
		List<Product> products = productService.getProducts(param,asc);
		if (products == null) {
            return new ResponseEntity<List<Product>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
	}
	
	
		
	//Get a product  by its product code
	@RequestMapping(path="/code/{pcode}")
	public ResponseEntity<Product> getProductByPcode(@PathVariable("pcode") int pcode) {
		logger.info("Fetching product with product_code {} ", pcode);
		Product product = productService.getProductByPcode(pcode); 
		
		if (product == null) {
			 logger.error("Product with product_code {} not found.", pcode);
			 return new ResponseEntity(new CustomErrorType("Product with product_code " + pcode + "not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Product>(product,HttpStatus.OK);
	}
	
	
	
	
	//Create a product
	
	@PostMapping(consumes = {"application/json"})
	public ResponseEntity<?> addProduct(@RequestBody Product product,UriComponentsBuilder ucBuilder) {
		
		logger.info("Creating product : {}", product);
		
		if (productService.getProductByPcode(product.getPcode())!= null) {
			logger.error("Unable to create. A Product with name {} already exist", product.getPname());
			return new ResponseEntity(new CustomErrorType("Unable to create. A Product with code " + 
			product.getPcode() + " already exist."),HttpStatus.CONFLICT);
		}
		System.out.println(productService.getProductByPcode(product.getPcode()));
		productService.save(product);
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.setLocation(ucBuilder.path("/product/{pid}").buildAndExpand(product.getPid()).toUri());
		return new ResponseEntity<String>(headers, HttpStatus.CREATED);
	}
	
	
	
    //Update a product 
	@RequestMapping(value="/{pcode}", consumes = {"application/json"}, method = {RequestMethod.GET, RequestMethod.PUT})
	public ResponseEntity<Product> updateProduct(@RequestBody Product product,@PathVariable("pcode") int pcode) {		
		logger.info("Updating Product with product_code {}", pcode);
		
		//Searching for the product with the code given by the client
		Product currentProduct = productService.getProductByPcode(pcode);
		
		if (currentProduct == null) {
			logger.error("Unable to update. Product with product_code {} not found.", pcode);
			return new ResponseEntity(new CustomErrorType("Unable to upate. Product with product_code: " + pcode + " not found."),
					HttpStatus.NOT_FOUND);
		}		
	
		//checks if the new code is occupied by another product
		Product existingProduct = productService.getProductByPcode(product.getPcode());
		if(existingProduct != null) {
			logger.error("Unable to update. Product with the same product_code {} has been found.", existingProduct.getPcode());
			return new ResponseEntity(new CustomErrorType("Unable to upate. Product with the same product_code: " + existingProduct.getPcode() + " has been found."),
					HttpStatus.CONFLICT);
		}
		productService.updateProduct(product, currentProduct);
		
		return new ResponseEntity<Product>(currentProduct, HttpStatus.OK);	
		
	}

	//Delete a product 
	@DeleteMapping(path="/{pcode}")
	public ResponseEntity<?> deleteProduct(@PathVariable("pcode") int pcode) {
		logger.info("Fetching & Deleting Product with product_code {}", pcode);
		Product product = productService.getProductByPcode(pcode);
		
		if (product == null) {
			logger.error("Unable to delete. Product with pcode {} not found.", pcode);
			return new ResponseEntity(new CustomErrorType("Unable to delete. Product with pcode " + pcode + " not found."),
					HttpStatus.NOT_FOUND);
		}
		productService.delete(product);
		
		return new ResponseEntity<Product>(HttpStatus.NO_CONTENT);
	}
	
	
	
	
}
