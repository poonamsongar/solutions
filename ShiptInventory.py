import pymysql


def check_prod_id_valid(prodId, found):
    with connection as db:
        cursor = db.connection.cursor()
        sql = "SELECT `id` FROM currInventory"
        # try:
        cursor.execute(sql)
        result = cursor.fetchall()
        for row in result:
            if str(row[0]) == str(prodId):
                found = 1
    return found


def print_cur_inventory():
    with connection as db:
        cursor = db.connection.cursor()
        sql = "SELECT `id`, `items`, `qty` FROM currInventory"
        # try:
        cursor.execute(sql)
        result = cursor.fetchall()

        print("Id\t\tItems\t\t\tQuantity")
        print("---------------------------------------------------------------------------")
        for row in result:
            print(str(row[0]) + "\t\t" + row[1] + "\t\t\t" + str(row[2]))
        print("What would you like to order today?")
        prodId = input("Please enter product id: ")
        # Need to check prodId is valid
        found = 0
        found = check_prod_id_valid(prodId, found)
        if int(found) == 1:
            qty = input("Please enter Qty: ")
            # Need to check qty is int only
            try:
                val = int(qty)
            except ValueError:
                print("Qty entered is not a valid number. Please start over")
                input("Press Enter to continue...")
                print_cur_inventory()
            read_qty(prodId, qty)
            print_cur_inventory()
        else:
            print("Product Id entered is invalid. Please start over")
            input("Press Enter to continue...")
            print_cur_inventory()


def update_qty(prod_id, updated_qty):
    with connection as db:
        cursor = db.connection.cursor()
        sql = "UPDATE currInventory SET `qty`=%s WHERE `id` = %s"
        cursor.execute(sql, (int(updated_qty), prod_id))
        print("Successfully Updated...")


def read_qty(prod_id, order_qty):
    with connection as db:
        cursor = db.connection.cursor()
        sql = "SELECT `qty` FROM currInventory WHERE id = " + prod_id
        cursor.execute(sql)
        result = cursor.fetchall()
        for row in result:
            if row[0] >= int(order_qty) and int(order_qty) > 0:
                # update currInventory
                updatedQty = row[0] - int(order_qty)
                update_qty(prod_id, updatedQty)
                print("----------- Your items have been ordered --------")
            else:
                print("Order qty is too large or not valid (negative or 0), please review inventory information below and start over")
                input("Press Enter to continue...")


try:
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='*****',
        db='shipt',
    )
    print_cur_inventory()

except pymysql.err.ProgrammingError as except_detail:
    print("pymysql.err.ProgrammingError: «{}»".format(except_detail))
    connection.commit()
finally:
    connection.close()
