## DDD-START

### 도메인
온라인 환경에서 책을 구매할 수 있도록 구성된 "온라인 서점"은 소프트웨어로 해결하고자 하는 문제 영역 즉 **도메인**에 해당된다.
하나의 도메인은 다시 하위 도메인으로 나눌 수 있는데 온라인으로 책을 판매 하는데 필요한 "책 조회", "구매", "배송" 각각의 도메인으로 나눌수 있다.

고객이 책을 주문하면 "책 조회", "구매", "배송" 하위 도메인의 기능이 엮이게 된다. 하나의 도메인은 다른 도메인과 연동하여 완전한 기능을 제공한다.
규모가 큰 온라인 서점은 "고객 할인 혜택"을 제공할 수 있겠지만 소규모 업체는 제공하지 않을 수 있다. 즉 도메인을 어떻게 구성할 지 여부는 주어진 상황에 따라 달라진다.

#### 도메인 모델 패턴
```text
% 애플리케이션 아키텍처
표현 계층 : 사용자에게 보여질 수 있도록 표현 (UI)
응용 계층 : 사용자가 요청한 기능을 실행. 업무 로직을 직접 구현하지 않으며 도메인 계층을 조합해서 기능을 실행
도메인 계층 : 시스템이 제공할 도메인의 규칙을 구현
인프라스트럭처 : 데이터베이스나 메시징 시스템과 같은 외부 시스템과의 연동을 처리
```
도메인 계층은 도메인의 핵심 규칙을 구현한다.
```java
/*
주문 도메인의 규칙
 1. 출고 전에 배송지를 변경할 수 있다.
*/
public class Order {

    private OrderState orderState;
    private ShippingInfo shippingInfo;

    public void changeShippingInfo(ShippingInfo newShippingInfo) {
        if (!orderState.isShippingChangeable()) {
            throw new IllegalStateException("배송 상태를 변경할 수 없습니다.");
        }
        
        this.shippingInfo = newShippingInfo;
    }
    
    public void changeShipped() {
        this.orderState = OrderState.SHIPPED;
    }
}

public enum OrderState {
    PAYMENT_WATTING {
        @Override
        public boolean isShippingChangeable() { // 주문 대기 중이거나 상품 준비중에는 배송지를 변경할 수 있다는 도메인 규칙을 구현
            return true;
        }
    },
    PREPARING {
        @Override
        public boolean isShippingChangeable() {
            return true;
        }
    },
    SHIPPED, DELIVERING, DELIVERY_COMPLETED;
    
    public boolean isShippingChangeable() {
        return false;
    }
}
```
주문과 관련된 규칙을 주문 도메인 모델인 Order와 OrderState에서만 구현하기에 규칙이 바뀌거나 확장해야 할 때 다른 코드에 영향을 주지 않는다.

#### 엔티티와 밸류
1. 엔티티
    - 엔티티의 가장 큰 특징은 식별자를 갖는다는 것. 식별자는 엔티티마다 고유하여 각 엔티티는 서로 다른 식별자를 가진다. (ex > 주문의 주문번호)
    - 식별자는 엔티티가 생성될 때 부여되어 삭제되기 전까지 바뀌지 않는다.
    - 엔티티의 식별자는 바뀌지 않고 고유하기 때문에 두 엔티티 객체의 식별자가 같으면 두 엔티티는 같다고 판단할 수 있다.
2. 밸류
    - 고유의 식별자를 갖지 않는다.
    - 개념적으로 하나의 타입을 표현할 때 사용한다.
    - ```java
      /*
        주소 관련 데이터를 개념적으로 하나의 밸류 타입으로 표현할 수 있다.
      */
      public class Address {
          private String address1;
          private String address2;
          private String zipcode;
      }
      ```
    - 밸류는 두 밸류 타입이 같은지 비교할 때는 모든 속성이 같은지 비교 해야한다.

#### 애그리거트
- 관련된 객체를 하나의 군으로 묶어 복잡한 도메인을 이해하고 관리하기 쉬운 단위로 만들어 좀 더 상위 수준에서 도메인 모델 간의 관계를 파악할 수 있도록 한다.  
- 모델을 이해하는데 도움을 주고 일관성을 관리하는 기준이 될수 있다.
- 하나의 애그리거트에 속한 객체는 유사하거나 동일한 라이프사이클을 가진다. (주문 애그리거트를 만들려면 Order, OrderLine, ShippingInfo와 같은 관련 객체들이 함께 생성해야 한다. Order 객체 없이 다른 객체들이 단독으로 생성되는 일은 없다.)
- 한 애그리거트에 속한 객체는 다른 애그리거트에 속하지 않는다. 각 애그리거트는 자기 자신을 관리할 뿐 다른 애그리거트를 관리하지 않는다.

###### 애그리거트 루트
- 애그리거트는 여러 객체로 구성되기 때문에 속해있는 모든 객체가 정상 상태를 가져야한다. 이를위해 전체를 관리할 주체가 필요한 데 이 책임을 지는것이 애그리거트 루트이다.
- 주문 애그리거트(Order, OrderLine, ShippingInfo)에서 애그리거트 루트는 Order이다.
- 애그리거트 루트의 핵심 역할은 애그리거트의 일관성이 깨지지 않도록 하는 것이다. 이를 위해 애그리거트 루트는 애그리거트가 제공해야 할 도메인 기능을 구현한다.
    - ```java
          // BAD : 애그리거트 루트(Order)가 아닌 다른 객체가 애그리거트에 속한 객체를 직접 변경.
          // 데이터 일관성이 깨지고 주소 변경 가능여부가 추가된다면 동일한 로직이 중복되서 구현되므로 유지보수를 어렵게한다.
          ShippingInfo si = order.getShippingInfo();
          si.setAddress(newAddress);
          ```
    - ```java
      // GOOD : 애그리거트 루트를 통해 애그리거트에 속한 객체를 변경
      public class Order {     
          private ShippingInfo shippingInfo;
  
          // 배송지를 변경 가능 여부를 확인하고 가능한 경우에만 배송지를 변경 
          public void changeShippingInfo(ShippingInfo newShippingInfo) {
              if (!orderState.isShippingChangeable()) {
                  throw new IllegalStateException("배송 상태를 변경할 수 없습니다.");
              } 
              this.shippingInfo = newShippingInfo;
          }
      }
      ```

###### 리포지터리와 애그리거트
- 객체의 영속성을 처리하는 리포지터리는 애그리거트 단위로 존재한다. 즉 Order와 OrderLine을 각각 별도 DB 테이블에 저장하지만 Order가 애그리거트 루트이도 OrderLine은 구성요소이므로 Order를 위한 리포지터리만 존재한다.
- 애그리거트는 개념적으로 하나이므로 리포짓터리는 애그리거트 전체를 저장소에 영속화 해야한다. (즉 Order가 영속화될 때 OrderLine, ShippingInfo도 같이 영속화 되어야 한다.)


##### Reference
DDD START!.최범균.지앤선
