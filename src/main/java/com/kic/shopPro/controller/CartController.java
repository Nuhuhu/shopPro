package com.kic.shopPro.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kic.shopPro.domain.CartVO;
import com.kic.shopPro.domain.ItemVO;
import com.kic.shopPro.domain.MemberVO;
import com.kic.shopPro.service.CartService;
import com.kic.shopPro.service.ItemService;

@Controller
public class CartController {
	@Autowired
	private CartService cartService;
	@Autowired
	private ItemService itemService;

	private static final Logger logger = LoggerFactory.getLogger(CartController.class);
	
	@RequestMapping(value = "/cart", method = RequestMethod.GET)
	public String cartPagePOST(Model model, HttpSession session) throws Exception {
		MemberVO get = (MemberVO) session.getAttribute("login");
		if (get == null) {
			String msg = "로그인이 되어있지 않습니다.";
			String url = "main";
			model.addAttribute("msg", msg);
			model.addAttribute("url", url);
			return "alert";
		}
		return "cart/cart";
	}

	@RequestMapping(value = "/cart", method = RequestMethod.POST)
	public String cartPageGET(Model model, @RequestParam("itemcount") int itemcount,
			@RequestParam("itemid") String itemid, HttpSession session) throws Exception {
		//사용자 세션 정보
		MemberVO get = (MemberVO) session.getAttribute("login");
		//사용자의 장바구니에서 해당 상품 목록
		CartVO cVO = cartService.readCartMethod(get.getId(), itemid);
		//상품 정보
		ItemVO iVO = itemService.readFoodItemByIdMethod(itemid);
		//장바구니에 해당 상품이 없을 때 새로 추가
		if (cVO == null) {
			cVO = new CartVO(get.getId(), iVO, itemcount);
			cartService.insertCartMethod(cVO);
		//장바구니에 해당 상품이 있을 때 기존 정보 업데이트
		} else {
			itemcount = cVO.getItemcount() + itemcount;
			cVO.setItemcount(itemcount);
			cVO.setPrice(itemcount * iVO.getPrice());
			cartService.updateCartMethod(cVO);
		}
		//사용자의 전체 장바구니 목록
		List<CartVO> cartList = cartService.readCartListMethod(get.getId());
		logger.info(cartList.toString());
		model.addAttribute("cartList", cartList);
		return "cart/cart";
	}

	
	@RequestMapping(value="/delete",method=RequestMethod.POST)
	public String deleteGET(Model model, HttpSession session, @RequestParam("itemid") String itemid) throws Exception{
		//사용자 세션 정보
		MemberVO get = (MemberVO) session.getAttribute("login");
		//상품 삭제
		cartService.deleteCartMethod(get.getId(), itemid);
		return "redirect: /shopPro/cart";
	} 
	
	@RequestMapping(value="/inputOrder",method=RequestMethod.GET)
	public String inputOrderGET(Model model, HttpSession session) throws Exception{
		MemberVO get = (MemberVO) session.getAttribute("login");
		List<CartVO> list = null;
		list = cartService.readCartListMethod(get.getId());
		cartService.inputOrderMethod(list);
		cartService.deleteAllCartMethod(get.getId());
		return "redirect: /shopPro/cart";
	}
}
